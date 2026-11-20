console.log("createTournament.js has loaded successfully!");

const totalSteps = 5;

// js DB logic start...

// This is perfectly valid, standard JavaScript
let currentStep = parseInt(document.getElementById("hiddenStep").value);

console.log("Current step initialized to:", currentStep);

// Call the UI update function once the page is ready
document.addEventListener("DOMContentLoaded", function () {
  updateWizardUI(); // Call this to show the correct step
});

function toggleBallType() {
  console.log("Ball type is called...");
  const sportSelect = document.getElementById("sportSelect");
  const ballTypeSection = document.getElementById("ballTypeSection");

  // Check if the selected value is exactly 'cricket'
  if (sportSelect.value === "cricket") {
    ballTypeSection.style.display = "block"; // Show the field
  } else {
    ballTypeSection.style.display = "none"; // Hide the field
    // Optional: clear the value so it doesn't save if they switch sports
    document.querySelector('select[name="ballType"]').value = "";
  }
}

// Global Sidebar Toggle
function toggleSidebar() {
  const sidebar = document.getElementById("sidebar");
  const overlay = document.getElementById("mobileOverlay");
  if (window.innerWidth <= 768) {
    sidebar.classList.toggle("mobile-open");
    overlay.classList.toggle("active");
  } else {
    sidebar.classList.toggle("collapsed");
  }
}

// Wizard Logic
function updateWizardUI() {
  document
    .querySelectorAll(".wizard-step")
    .forEach((step) => step.classList.remove("active"));
  document.getElementById(`step-${currentStep}`).classList.add("active");

  document.querySelectorAll(".step-item").forEach((item, index) => {
    const stepNum = index + 1;
    if (stepNum <= currentStep) {
      item.classList.add("active");
    } else {
      item.classList.remove("active");
    }
  });

  const nextBtn = document.querySelector(".btn-save-next");
  const cancelBtn = document.querySelector(".btn-cancel");

  if (currentStep === totalSteps) {
    nextBtn.textContent = "Publish Tournament";
  } else {
    nextBtn.textContent = "Save & Next";
  }

  if (currentStep > 1) {
    cancelBtn.textContent = "Back";
  } else {
    cancelBtn.textContent = "Cancel";
  }
}

document.addEventListener("DOMContentLoaded", function () {
  // Select the specific dropdowns by their name
  const selectsToClear = document.querySelectorAll(
    'select[name="sport"], select[name="tournamentType"], select[name="ballType"]'
  );

  selectsToClear.forEach((select) => {
    select.value = ""; // Force the value to empty
  });
});

// This function finds the step automatically and saves
function saveCurrentStep() {
  console.log("Save Current Step....");

  // 1. Find the currently active step div
  const activeStepDiv = document.querySelector(".wizard-step.active");

  // 2. Extract the number from the ID (e.g., "step-1" becomes 1)
  const stepNumber = parseInt(activeStepDiv.id.split("-")[1]);

  console.log("The step Number is : ", stepNumber);
  // 3. Call your save function with that number
  saveStep(stepNumber);
}
async function saveStep(stepNumber) {
  // 1. Gather all data first
  // const data = gatherStepData(stepNumber);

  const form = document.getElementById("tournamentForm");
  const formData = new FormData(form);
  formData.append("currentStep", stepNumber);

  // 3. If it's Step 5, append the file
  if (stepNumber === 5) {
    formData.append("action", "PUBLISH");
    const fileInput = document.getElementById("fileInput");
    if (fileInput && fileInput.files.length > 0) {
      formData.append("bannerImage", fileInput.files[0]);
    }
  }

  // 4. Send the request
  // IMPORTANT: Do NOT manually set Content-Type;
  // the browser sets it automatically with the correct boundary for FormData.
  const response = await fetch("/api/organizer/tournament/save-step", {
    method: "POST",
    body: formData,
  });

  // 5. Handle response
  if (!response.ok) {
    let errorMessage = "A server error occurred. Please try again.";

    try {
      // Try to get the specific message from the server's error response
      const errorData = await response.json();
      if (errorData.message) {
        errorMessage = errorData.message;
      }
    } catch (e) {
      console.error("Could not parse error JSON", e);
    }

    showSweetAlert(errorMessage, "red");
    return;
  }

  const result = await response.json();

  if (response.ok && result.success) {
    if (stepNumber < 5) {
      // AUTOMATIC REDIRECT:
      // Instead of just calling updateWizardUI(), force a navigation to the next step
      // This triggers the @GetMapping route, which updates the DB and reloads the page
      window.location.href = result.nextUrl;
    } else {
      const swalResult = await showSweetAlert(
        "Tournament Published successfully!",
        "green"
      );

      // 2. Now check the result from that specific interaction
      if (swalResult.isConfirmed) {
        window.location.href = "/api/organizer/dashboard";
      }
    }
  } else if (!result.success && result.requiresIdentity) {
    // Show the modal instead of a simple error alert
    showIdentityModal(result.message);
  } else {
    // Error Path (Includes Validation errors and Duplicate Name errors)
    const errorMessage =
      result.message || "Failed to save. Please check your inputs.";
    showSweetAlert(errorMessage, "red");
  }
  console.log("Proceeding to save...");
}
function showIdentityModal(message) {
  return Swal.fire({
    icon: "warning",
    title: "Identity Verification Required",
    text: message,
    background: "#1f2937",
    color: "#ffffff",
    showCancelButton: true,
    confirmButtonText: "Complete Identity",
    cancelButtonText: "Cancel",
    confirmButtonColor: "#ea580c",
    cancelButtonColor: "#4b5563",
    customClass: {
      popup: "rounded-xl",
    },
  }).then((result) => {
    // If the user clicks "Complete Identity"
    if (result.isConfirmed) {
      window.location.href = "/api/organizer/Identity-Info";
    }
    // If they click "Cancel", the popup automatically closes
  });
}
function showSweetAlert(content, type) {
  let swalIcon = "info";
  let swalTitle = "Notice";

  if (type === "red") {
    swalIcon = "error";
    swalTitle = "Oops...";
  } else if (type === "green") {
    swalIcon = "success";
    swalTitle = "Success!";
  }

  return Swal.fire({
    icon: swalIcon,
    title: swalTitle,
    text: content,
    background: "#1f2937",
    color: "#ffffff",
    confirmButtonColor: "#ea580c",
    customClass: { popup: "rounded-xl" },
    // This ensures the popup waits for user interaction
    allowOutsideClick: false,
  });
}
function gatherStepData(step) {
  console.log("Gather Step is called...");

  let data = {};

  data.id = document.getElementById("tournamentId").value;

  console.log(
    "The tournament id : ",
    document.getElementById("tournamentId").value
  );

  if (step == 1) {
    data.tournamentName = document.querySelector(
      '[name="tournamentName"]'
    ).value;
    data.tournamentType = document.querySelector(
      '[name="tournamentType"]'
    ).value;
    data.sport = document.querySelector('[name="sport"]').value;
    data.venueLocation = document.querySelector('[name="venueLocation"]').value;
    data.startDate = document.querySelector('[name="startDate"]').value;
    data.endDate = document.querySelector('[name="endDate"]').value;

    console.log("The tournament name is : ", data.tournamentName);
    console.log("The Tournament type is : ", data.tournamentType);
  } else if (step == 2) {
    data.entryFee = document.querySelector('[name="entryFee"]').value;
    data.maxTeamsAllowed = document.querySelector(
      '[name="maxTeamsAllowed"]'
    ).value;
    data.registrationStartDate = document.querySelector(
      '[name="registrationStartDate"]'
    ).value;
    data.registrationDeadline = document.querySelector(
      '[name="registrationDeadline"]'
    ).value;
    data.playerCategory = document.querySelector(
      '[name="playerCategory"]'
    ).value;
  } else if (step == 3) {
    data.tournamentStyle = document.querySelector(
      '[name="tournamentStyle"]'
    ).value;
    data.minSquadSize = document.querySelector('[name="minSquadSize"]').value;
    data.maxSquadSize = document.querySelector('[name="maxSquadSize"]').value;

    const ruleItems = document.querySelectorAll("#rulesContainer .rule-item");
    const rulesArray = [];

    ruleItems.forEach((item) => {
      // Find the hidden input inside the rule-item
      const input = item.querySelector('input[type="hidden"]');
      if (input && input.value) {
        rulesArray.push({
          description: input.value,
        });
      }
    });

    data.customRules = rulesArray;
  } else if (step === 4) {
    // This calls the function you already wrote!
    data.prizes = capturePrizes();
  }
}
function capturePrizes() {
  const prizeRows = document.querySelectorAll("#prizeRowList .prize-row");
  const prizesArray = [];

  prizeRows.forEach((row) => {
    // Simple defensive programming: check if elements exist before reading .value
    const nameInput = row.querySelector(".name-input");
    const typeSelect = row.querySelector(".row-select");
    const valueInput = row.querySelector(".value-input");

    if (nameInput && typeSelect && valueInput) {
      prizesArray.push({
        name: nameInput.value,
        type: typeSelect.value,
        value: valueInput.value,
      });
    }
  });

  console.log("Captured Prizes for Submission:", prizesArray);
  return prizesArray;
}
// file..
document.getElementById("fileInput").addEventListener("change", function () {
  if (this.files.length > 0) {
    // Find the <p> tag inside the box and update it
    const boxText = this.parentElement.querySelector("p");
    boxText.innerText = "Selected: " + this.files[0].name;
    boxText.style.color = "#ff8c00"; // Optional: Change color to show success
  }
});
function goBack() {
  if (currentStep > 1) {
    currentStep--;
    updateWizardUI();
  } else {
    window.location.href = "dashboard.html";
  }
}

let prizeCount = document.querySelectorAll(".prize-row").length;
// Add Prize Row
document.getElementById("addPrizeBtn").addEventListener("click", function () {
  const list = document.getElementById("prizeRowList");
  const newRow = document.createElement("div");
  newRow.className = "prize-row";

  // Use the index (prizeCount) to generate unique names for Spring binding
  newRow.innerHTML = `
        <input type="text" name="prizes[${prizeCount}].prizeName" class="row-input name-input" placeholder="Prize Name" />
        <div class="select-wrap">
            <select name="prizes[${prizeCount}].prizeType" class="row-select">
                <option value="Cash">Cash</option>
                <option value="Trophy">Trophy</option>
                <option value="Gear">Gear</option>
            </select>
        </div>
        <input type="text" name="prizes[${prizeCount}].prizeDetails" class="row-input value-input" placeholder="Value / Details" />
        <button type="button" class="row-delete-btn" onclick="this.parentElement.remove()"><i class="fa-solid fa-trash"></i></button>
    `;
  list.appendChild(newRow);
  prizeCount++;
});

// Remove Prize Row
document.getElementById("prizeRowList").addEventListener("click", function (e) {
  const btn = e.target.closest(".row-delete-btn");
  if (btn) {
    btn.closest(".prize-row").remove();
  }
});

//Step 3: Match Format & Rules
let rules = [];

// Ensure this runs after the page loads
document.addEventListener("DOMContentLoaded", () => {
  const addBtn = document.getElementById("addRuleBtn");
  if (addBtn) {
    addBtn.addEventListener("click", addRule);
  }
});
// 1. Add Rule Function
window.addRule = function () {
  const input = document.getElementById("rule-Input");
  const val = input.value.trim();
  if (!val) return;

  const container = document.getElementById("rulesContainer");
  const index = container.getElementsByClassName("rule-item").length;

  const newRow = document.createElement("div");
  newRow.className = "rule-item";
  newRow.innerHTML = `
        <span class="rule-text">${index + 1}. ${val}</span>
        <input type="hidden" name="customRules[${index}].description" value="${val}" />
        <div class="rule-actions">
            <button type="button" class="btn-edit" onclick="editRow(this)"><i class="fa-solid fa-pencil"></i></button>
            <button type="button" class="btn-del" onclick="removeRow(this)"><i class="fa-solid fa-trash"></i></button>
        </div>
    `;
  container.appendChild(newRow);
  input.value = "";
};

// 2. Remove Rule Function
window.removeRow = function (button) {
  const row = button.closest(".rule-item");
  row.remove();

  // Re-index everything in the container
  document.querySelectorAll("#rulesContainer .rule-item").forEach((row, i) => {
    const span = row.querySelector("span");
    const input = row.querySelector("input[type='hidden']");
    span.innerText = i + 1 + ". " + input.value;
    input.name = `customRules[${i}].description`;
  });
};

function toggleEdit(index) {
  const list = document.getElementById("rulesList");
  const items = list.getElementsByClassName("rule-item");
  items[index].innerHTML = `
    <input type="text" class="rule-edit-input" id="edit-${index}" value="${rules[index]}" />
    <div>
        <button type="button" class="btn-action btn-save-rule" onclick="saveEdit(${index})"><i class="fa-solid fa-check"></i></button>
    </div>
`;
}

function editRow(index) {
  const newValue = document.getElementById(`edit-${index}`).value;
  if (newValue.trim()) {
    rules[index] = newValue.trim();
  }
  renderRules();
}

window.editRow = function (button) {
  const row = button.closest(".rule-item");
  const span = row.querySelector("span");
  const input = row.querySelector("input");
  const actions = row.querySelector(".rule-actions");

  // 1. Hide the span and the pencil icon
  span.style.display = "none";
  button.style.display = "none";

  // 2. Create an inline input field
  const editInput = document.createElement("input");
  editInput.type = "text";
  editInput.className = "rule-input-inline";
  editInput.value = input.value;
  editInput.style.flex = "1";
  editInput.style.marginRight = "10px";

  // 3. Create a "Save" button
  const saveBtn = document.createElement("button");
  saveBtn.innerHTML = '<i class="fa-solid fa-check"></i>';
  saveBtn.className = "btn-save";
  saveBtn.onclick = function () {
    const newValue = editInput.value.trim();
    if (newValue) {
      input.value = newValue;
      span.innerText = span.innerText.split(".")[0] + ". " + newValue;
    }
    // Restore view
    span.style.display = "block";
    button.style.display = "inline-block";
    editInput.remove();
    saveBtn.remove();
  };

  // 4. Insert the new elements into the row
  row.insertBefore(editInput, actions);
  row.insertBefore(saveBtn, actions);
  editInput.focus();
};

function renderRules() {
  const container = document.getElementById("rulesContainer");

  const index = document.querySelectorAll(".rule-input-group").length;
  const newDiv = document.createElement("div");
  newDiv.className = "rule-input-group";

  newDiv.innerHTML = `
    <input type="text" 
              name="customRules[${index}].description" 
              class="rule-input" 
              value="${rules[rules.length - 1]}" />
        <button type="button" class="btn-del" onclick="removeRow(this)">Delete</button>
  `;
}

window.logRulesToConsole = function () {
  const rules = [];
  // Select all hidden inputs that hold the rule descriptions
  const inputs = document.querySelectorAll(
    '#rulesContainer input[type="hidden"]'
  );

  inputs.forEach((input, index) => {
    rules.push({
      index: index,
      name: input.name,
      value: input.value,
    });
  });

  console.log("--- Rules to be sent to Controller ---");
  console.table(rules); // console.table displays it in a nice readable format
};

window.addRule = addRule;
window.removeRule = removeRule;
window.toggleEdit = toggleEdit;
window.saveEdit = saveEdit;
window.saveCurrentStep = saveCurrentStep;
