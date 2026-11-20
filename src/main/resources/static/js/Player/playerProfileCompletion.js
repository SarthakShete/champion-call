console.log("Player Profile completion is called....");

let currentStep = 1;
let totalSteps = 4;

// Add a variable to track status
let isSaving = false;

document.addEventListener("DOMContentLoaded", function () {
  // 1. Initialize Step from Hidden Input
  currentStep = parseInt(document.getElementById("hiddenStep").value) || 1;

  // 2. Initial UI setup
  goToStep(currentStep);

  updateWizardUI(); // Make sure to call this to set the buttons correctly!

  // 3. Attach Save & Next Listeners
  document.querySelectorAll(".step-save-next-btn").forEach((btn) => {
    btn.addEventListener("click", () => {
      if (validateStep(currentStep)) {
        saveStep(currentStep);
      }
    });
  });

  // 4. Final Save Button
  document.getElementById("finalSaveBtn").addEventListener("click", () => {
    if (validateStep(4)) {
      saveStep(4, 4); // 4 = final step
    }
  });

  // 5. Back Button Listener
  document.querySelectorAll(".step-back-btn").forEach((btn) => {
    btn.addEventListener("click", () => {
      currentStep = parseInt(btn.dataset.goto);
      goToStep(currentStep);
      updateWizardUI();
    });
  });

  // 6. ID Document Upload -> filename display (Step 3)
  const idDocumentInput = document.getElementById("idDocument");
  if (idDocumentInput) {
    idDocumentInput.addEventListener("change", function (event) {
      const fileNameDisplay = document.getElementById("fileNameDisplay");
      const file = event.target.files[0];
      if (file) {
        fileNameDisplay.textContent = file.name;
        fileNameDisplay.style.display = "block";
      } else {
        fileNameDisplay.textContent = "";
        fileNameDisplay.style.display = "none";
      }
    });
  }
});

// Navigation Logic
function goToStep(stepNumber) {
  document.querySelectorAll(".onboard-step").forEach((step) => {
    step.style.display =
      parseInt(step.dataset.step) === stepNumber ? "block" : "none";
  });

  // Update tracker dots (Optional: add logic here if you want it)
  currentStep = stepNumber;
}

function updateWizardUI() {
  // Show current step, hide others
  document.querySelectorAll(".onboard-step").forEach((step) => {
    step.style.display =
      parseInt(step.dataset.step) === currentStep ? "block" : "none";
  });

  // Update Step Tracker Dots
  document.querySelectorAll(".step-dot").forEach((dot) => {
    const dotStep = parseInt(dot.dataset.step);
    dot.classList.toggle("active", dotStep === currentStep);
    dot.classList.toggle("done", dotStep < currentStep);
  });

  // Update Buttons
  const activeStepEl = document.querySelector(
    `.onboard-step[data-step="${currentStep}"]`
  );
  const nextBtn = activeStepEl
    ? activeStepEl.querySelector(".btn-primary-action")
    : null;
  if (nextBtn) {
    nextBtn.textContent =
      currentStep === totalSteps ? "Complete Profile" : "Save & Next";
  }
}
// Validation Logic
function validateStep(step) {
  const stepEl = document.querySelector(`.onboard-step[data-step="${step}"]`);
  const inputs = stepEl.querySelectorAll("[required]");
  for (let input of inputs) {
    if (!input.checkValidity()) {
      input.reportValidity();
      return false;
    }
  }
  return true;
}
// ==========================================
// DATA GATHERING (The "Standard Way")
// ==========================================
function gatherStepData(step) {
  let data = {};
  if (step === 1) {
    data.fullName = document.getElementById("fullName").value;
    data.username = document.getElementById("username").value;
    data.dateOfBirth = document.getElementById("dob").value;
    data.city = document.getElementById("city").value;
    data.phoneNumber = document.getElementById("phoneNumber").value;
  } else if (step === 2) {
    data.primarySport = document.getElementById("primarySport").value;
    data.playerRole = document.getElementById("playerRole").value;
    data.skillLevel = document.getElementById("skillLevel").value;
    data.preferredSide = document.getElementById("preferredSide").value;
  } else if (step === 3) {
    data.idType = document.getElementById("idType").value;
    data.idNumber = document.getElementById("idNumber").value;
    // idDocument file itself is picked up directly from FormData in saveStep()
  } else if (step === 4) {
    data.bio = document.getElementById("bio").value;
    data.yearsOfExperience = document.getElementById("yearsOfExperience").value;
    data.currentStatus = document.getElementById("currentStatus").value;
    data.otherStrength = document.getElementById("otherStrength").value;

    // Gather selected strengths
    const checkedStrengths = document.querySelectorAll(
      'input[name="strengthIds"]:checked'
    );
    data.strengthIds = Array.from(checkedStrengths).map((cb) => cb.value);
  }
  return data;
}

async function saveStep(stepNumber) {
  if (isSaving) return;
  isSaving = true;

  // 1. Show Loading Spinner only for the final step
  // if (stepNumber === totalSteps) {
  //   Swal.fire({
  //     title: "Saving...",
  //     allowOutsideClick: false,
  //     background: "#1f2937",
  //     color: "#ffffff",
  //     didOpen: () => {
  //       Swal.showLoading();
  //     },
  //   });
  // }

  const form = document.getElementById("onboardForm");
  const formData = new FormData(form);
  formData.append("currentStep", stepNumber);

  // OPTIMIZATION: Only send the profile photo if it is the final step
  // OR if a new file was actually selected by the user
  const fileInput = document.getElementById("photoUpload");
  if (stepNumber < totalSteps && fileInput.files.length === 0) {
    formData.delete("profilePhoto"); // Remove the empty file input from FormData
  }

  // Only send the ID document when the user is on/past Step 3
  // and has actually selected a file
  const idDocumentInput = document.getElementById("idDocument");
  if (idDocumentInput && idDocumentInput.files.length === 0) {
    formData.delete("idDocument");
  }

  try {
    const response = await fetch("/api/player/save-step", {
      method: "POST",
      body: formData,
    });

    const result = await response.json();

    if (stepNumber === totalSteps) Swal.close();

    if (response.ok && result.success) {
      if (stepNumber === totalSteps) {
        await Swal.fire({
          icon: "success",
          title: "Profile Completed!",
          text: "Your profile is ready.",
          background: "#1f2937",
          color: "#ffffff",
          confirmButtonColor: "#ea580c",
        });
        window.location.href = "/home";
      } else {
        currentStep = stepNumber + 1;
        goToStep(currentStep);
        updateWizardUI();
      }
    } else {
      Swal.fire({
        icon: "error",
        title: "Oops...",
        text: result.message || "Failed to save.",
        background: "#1f2937",
        color: "#ffffff",
      });
    }
  } catch (error) {
    if (stepNumber === totalSteps) Swal.close();
    console.error("Fetch Error:", error);
    Swal.fire({
      icon: "error",
      title: "Connection Error",
      text: "Please check your internet connection.",
      background: "#1f2937",
      color: "#ffffff",
    });
  } finally {
    isSaving = false;
  }
}
// Attach listener to Save & Next
document.querySelectorAll(".step-save-next-btn").forEach((btn) => {
  btn.addEventListener("click", () => {
    saveStep(currentStep);
  });
});

// Player Profile Image
// Function to update the initials based on the Full Name
function updateInitials() {
  const fullName = document.getElementById("fullName").value.trim();
  const initialsElement = document.getElementById("avatarInitials");

  if (fullName) {
    const names = fullName.split(" ");
    let initials = "";

    if (names.length >= 2) {
      // First letter of first name + First letter of last name
      initials = names[0].charAt(0) + names[names.length - 1].charAt(0);
    } else if (names.length === 1) {
      // Just first letter
      initials = names[0].charAt(0);
    }
    initialsElement.innerText = initials.toUpperCase();
  } else {
    initialsElement.innerText = "??"; // Fallback
  }
}

// 1. Listen for Full Name changes
document.getElementById("fullName").addEventListener("input", () => {
  const avatarPreview = document.getElementById("avatarPreview");
  // Only update initials if no image is currently displayed
  if (avatarPreview.style.display === "none") {
    updateInitials();
  }
});

// 2. Listen for Photo Upload
document
  .getElementById("photoUpload")
  .addEventListener("change", function (event) {
    const avatarCircle = document.getElementById("avatarCircle");
    const avatarInitials = document.getElementById("avatarInitials");
    const avatarPreview = document.getElementById("avatarPreview");

    const file = event.target.files[0];
    if (file) {
      const reader = new FileReader();
      reader.onload = function (e) {
        avatarPreview.src = e.target.result;
        avatarPreview.style.display = "block"; // Show image
        avatarInitials.style.display = "none"; // Hide initials
      };
      reader.readAsDataURL(file);
    }
  });
// ==========================================
// STEP 2 - SPORT -> ROLE DYNAMIC DROPDOWN
// ==========================================
const sportSelect = document.getElementById("primarySport");
const roleSelect = document.getElementById("playerRole");

const roles = {
  CRICKET: ["Batsman", "Bowler", "All-rounder", "Wicket-keeper", "Captain"],
  FOOTBALL: [
    "Goalkeeper",
    "Center-back",
    "Full-back",
    "Defensive Midfielder",
    "Central Midfielder",
    "Attacking Midfielder",
    "Left Winger",
    "Right Winger",
    "Striker",
  ],
  KABADDI: [
    "Raider",
    "All-rounder",
    "Left Corner",
    "Right Corner",
    "Left Cover",
    "Right Cover",
  ],
};

function updateRoles() {
  const selectedSport = sportSelect.value;
  const availableRoles = roles[selectedSport] || [];
  roleSelect.innerHTML =
    '<option value="" disabled selected>Select your role</option>';
  availableRoles.forEach((role) => {
    const option = document.createElement("option");
    option.value = role.toUpperCase().replace(/ /g, "_");
    option.textContent = role;
    roleSelect.appendChild(option);
  });
}

sportSelect.addEventListener("change", () => {
  updateRoles();
  populateStrengths(sportSelect.value);
});

// ==========================================
// STEP 4 - SPORT-AWARE KEY STRENGTHS
// Note: 'strengthsData' is globally defined in your HTML template
// ==========================================
const selectedStrengths = {
  CRICKET: new Set(),
  FOOTBALL: new Set(),
  KABADDI: new Set(),
};

function populateStrengths(sport) {
  const grid = document.getElementById("strengthGrid");
  if (!grid) return;
  grid.innerHTML = "";

  const sportStrengths = strengthsData[sport];
  if (!sport || !sportStrengths) {
    grid.innerHTML =
      '<p class="strength-empty-note">Select a sport to see relevant strengths.</p>';
    return;
  }

  sportStrengths.forEach((strengthObj) => {
    const isChecked = selectedStrengths[sport].has(strengthObj.id);
    const card = document.createElement("label");
    card.className = "strength-card" + (isChecked ? " checked" : "");

    card.innerHTML = `
      <input type="checkbox" name="strengthIds" value="${strengthObj.id}" ${
      isChecked ? "checked" : ""
    }>
      <span>${strengthObj.name}</span>
    `;

    card.querySelector("input").addEventListener("change", (e) => {
      card.classList.toggle("checked", e.target.checked);
      if (e.target.checked) selectedStrengths[sport].add(strengthObj.id);
      else selectedStrengths[sport].delete(strengthObj.id);
    });

    grid.appendChild(card);
  });
}

// Initial Run
goToStep(1);
