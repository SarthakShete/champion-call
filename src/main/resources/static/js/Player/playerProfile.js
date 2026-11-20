console.log("HIii player Profile....");
// ==========================================
// TOP-LEVEL ELEMENT REFERENCES (declared once)
// ==========================================
const fileInput = document.getElementById("profileImageInput");
const editModal = document.getElementById("editProfileModal");
const imageModal = document.getElementById("imageModal");
const closeEditBtn = document.getElementById("closeEditProfileBtn");
const cancelEditBtn = document.getElementById("cancelEditProfileBtn");
const editProfileForm = document.getElementById("editProfileForm");
const menuBtn = document.querySelector(".menu-btn");
const sidebar = document.querySelector(".sidebar");
const layout = document.querySelector(".layout");
const cropContainer = document.getElementById("waCropContainer");
const previewImg = document.getElementById("modalImagePreview");
const zoomSlider = document.getElementById("waZoomSlider");

document.addEventListener("DOMContentLoaded", () => {
  console.log("Player Profile is called...");
  console.log("DOM fully loaded, wiring up event listeners...");

  // ---- Sidebar hamburger toggle ----
  if (menuBtn && sidebar) {
    menuBtn.addEventListener("click", () => {
      sidebar.classList.toggle("active");
    });
  }

  if (menuBtn && layout) {
    menuBtn.addEventListener("click", () => {
      layout.classList.toggle("sidebar-hidden");
    });
  }

  // ---- 1. "Edit Profile" pencil buttons -> open TEXT modal ----
  const editButtons = document.querySelectorAll(".openEditProfileBtn");

  editButtons.forEach((btn) => {
    btn.addEventListener("click", (e) => {
      e.preventDefault();
      console.log("Edit button clicked -> opening file picker for image crop");
      if (fileInput) {
        fileInput.click();
      }
    });
  });

  // ---- 2. Click on profile PHOTO -> open native file picker ----
  const playerImages = document.querySelectorAll(".player-left img");
  console.log("Player Image ", playerImages);

  playerImages.forEach((img) => {
    img.style.cursor = "pointer";
    img.title = "Click to change profile picture";

    img.addEventListener("click", (e) => {
      e.stopPropagation();
      console.log("Profile photo image clicked -> opening file picker");
      if (fileInput) {
        fileInput.click();
      }
    });
  });
});

// ==========================================
// PLAYER PROFILE DATA
// ==========================================
let playerProfileData = {
  name: "Raj Kumar",
  location: "Mumbai, India",
  joined: "March 2023",
  sport: "Cricket",
  position: "Batsman, Captain",
  experience: "Pro",
  jersey: "#7",
  phone: "+91 9876543210",
  email: "raj.kumar@gmail.com",
  dob: "1998-02-14",
  address: "Andheri West, Mumbai, Maharashtra",
  idProofStatus: "Uploaded",
  verificationStatus: "Verified",
  teams: ["Mumbai Titans", "Pune Strikers"],
  payment: "UPI raj.kumar@okhdfc",
};

function maskPhone(phone) {
  return phone.slice(0, 6) + "******" + phone.slice(-2);
}
function maskPayment(payment) {
  return payment.replace(/(UPI\s)([\w.]{4})[\w.]*(@\w+)/, "$1****$3");
}
function formatDobDisplay(dobValue) {
  return new Date(dobValue).toLocaleDateString("en-GB", {
    day: "2-digit",
    month: "short",
    year: "numeric",
  });
}
function capitalize(str) {
  return str.charAt(0).toUpperCase() + str.slice(1);
}

function renderProfileView() {
  const p = playerProfileData;
  const payEl = document.getElementById("viewPayment");
  const googleEmailEl = document.getElementById("linkedGoogleEmail");

  if (payEl && p.payment) payEl.innerText = maskPayment(p.payment);
  if (googleEmailEl && p.email) googleEmailEl.innerText = p.email;
}

function fillEditForm() {
  const p = playerProfileData;
  const inputName = document.getElementById("inputName");
  const profileName = document.getElementById("profileName");
  const inputSport = document.getElementById("inputSport");
  const viewSport = document.getElementById("viewSport");

  if (inputName && profileName) inputName.value = profileName.innerText;
  if (inputSport && viewSport) inputSport.value = viewSport.innerText;
}

// ==========================================
// TEXT MODAL HANDLERS
// ==========================================
if (closeEditBtn && editModal) {
  closeEditBtn.addEventListener("click", () => {
    editModal.style.display = "none";
  });
}
if (cancelEditBtn && editModal) {
  cancelEditBtn.addEventListener("click", () => {
    editModal.style.display = "none";
  });
}

if (editProfileForm) {
  editProfileForm.addEventListener("submit", (e) => {
    e.preventDefault();

    playerProfileData = {
      ...playerProfileData,
      name:
        document.getElementById("inputName")?.value || playerProfileData.name,
      location:
        document.getElementById("inputLocation")?.value ||
        playerProfileData.location,
      sport:
        document.getElementById("inputSport")?.value || playerProfileData.sport,
      position:
        document.getElementById("inputPosition")?.value ||
        playerProfileData.position,
      experience:
        document.getElementById("inputExperience")?.value ||
        playerProfileData.experience,
      jersey:
        document.getElementById("inputJersey")?.value ||
        playerProfileData.jersey,
      phone:
        document.getElementById("inputPhone")?.value || playerProfileData.phone,
      email:
        document.getElementById("inputEmail")?.value || playerProfileData.email,
      dob: document.getElementById("inputDob")?.value || playerProfileData.dob,
      address:
        document.getElementById("inputAddress")?.value ||
        playerProfileData.address,
      payment:
        document.getElementById("inputPayment")?.value ||
        playerProfileData.payment,
    };

    renderProfileView();
    if (editModal) editModal.style.display = "none";
  });
}

// ==========================================
// PER-FIELD INLINE EDIT
// ==========================================
function enterFieldEditMode(field) {
  const viewEl = document.getElementById("view" + capitalize(field));
  const inputEl = document.getElementById("edit" + capitalize(field));
  const actionsEl = document.getElementById("actions" + capitalize(field));

  if (!viewEl || !inputEl || !actionsEl) return;

  inputEl.value = playerProfileData[field];
  viewEl.style.display = "none";
  inputEl.style.display = "block";

  actionsEl.innerHTML = `
    <button class="icon-btn field-save-btn" data-field="${field}"><i class="fa-solid fa-check"></i></button>
    <button class="icon-btn field-cancel-btn" data-field="${field}"><i class="fa-solid fa-xmark"></i></button>
  `;

  actionsEl
    .querySelector(".field-save-btn")
    .addEventListener("click", () => saveField(field));
  actionsEl
    .querySelector(".field-cancel-btn")
    .addEventListener("click", () => exitFieldEditMode(field));
}

function exitFieldEditMode(field) {
  const viewEl = document.getElementById("view" + capitalize(field));
  const inputEl = document.getElementById("edit" + capitalize(field));
  const actionsEl = document.getElementById("actions" + capitalize(field));

  if (!viewEl || !inputEl || !actionsEl) return;

  viewEl.style.display = "block";
  inputEl.style.display = "none";

  actionsEl.innerHTML = `
    <button class="icon-btn field-edit-btn" data-field="${field}"><i class="fa-solid fa-pen"></i></button>
  `;
  actionsEl
    .querySelector(".field-edit-btn")
    .addEventListener("click", () => enterFieldEditMode(field));
}

document.querySelectorAll(".field-edit-btn").forEach((btn) => {
  btn.addEventListener("click", () => enterFieldEditMode(btn.dataset.field));
});

// ==========================================
// SETTINGS DATA + HANDLERS
// ==========================================
let settingsData = {
  emailNotif: true,
  pushNotif: true,
  reminders: true,
  marketing: false,
  publicProfile: true,
  contactVisibility: true,
};

function bindSettingsToggle(elementId, key) {
  const el = document.getElementById(elementId);
  if (el) {
    el.checked = settingsData[key];
    el.addEventListener("change", () => {
      settingsData[key] = el.checked;
    });
  }
}

bindSettingsToggle("toggleEmailNotif", "emailNotif");
bindSettingsToggle("togglePushNotif", "pushNotif");
bindSettingsToggle("toggleReminders", "reminders");
bindSettingsToggle("toggleMarketing", "marketing");
bindSettingsToggle("togglePublicProfile", "publicProfile");
bindSettingsToggle("toggleContactVisibility", "contactVisibility");

const manageGoogleBtn = document.getElementById("manageGoogleBtn");
if (manageGoogleBtn) {
  manageGoogleBtn.addEventListener("click", () => {
    window.open("https://myaccount.google.com/", "_blank");
  });
}

const deactivateAccountBtn = document.getElementById("deactivateAccountBtn");
if (deactivateAccountBtn) {
  deactivateAccountBtn.addEventListener("click", () => {
    if (confirm("Are you sure you want to deactivate your account?")) {
      alert("Account deactivated (demo only).");
    }
  });
}

const deleteAccountBtn = document.getElementById("deleteAccountBtn");
if (deleteAccountBtn) {
  deleteAccountBtn.addEventListener("click", () => {
    if (
      confirm(
        "This will permanently delete your account and all data. Continue?"
      )
    ) {
      alert("Account deleted (demo only).");
    }
  });
}

// ==========================================
// VIEW SWITCHING & TOURNAMENT CLICK HANDLERS
// ==========================================
const menuItems = document.querySelectorAll(".sidebar .menu li[data-view]");
const pageViews = document.querySelectorAll(".page-view");

const ACTIVE_VIEW_STORAGE_KEY = "playerProfileActiveView";

function showView(viewId, skipSave) {
  pageViews.forEach((view) => {
    view.style.display = view.id === viewId ? "" : "none";
  });
  menuItems.forEach((item) => {
    item.classList.toggle("active", item.dataset.view === viewId);
  });
  window.scrollTo({ top: 0, behavior: "smooth" });

  if (!skipSave) {
    try {
      localStorage.setItem(ACTIVE_VIEW_STORAGE_KEY, viewId);
    } catch (e) {
      console.warn("Could not save active view to localStorage:", e);
    }
  }
}

menuItems.forEach((item) => {
  item.addEventListener("click", (e) => {
    e.preventDefault();
    showView(item.dataset.view);
  });
});

const backToOverviewLink = document.getElementById("backToOverviewLink");
if (backToOverviewLink) {
  backToOverviewLink.addEventListener("click", (e) => {
    e.preventDefault();
    showView("overview-view");
  });
}

(function restoreActiveViewOnLoad() {
  try {
    const savedViewId = localStorage.getItem(ACTIVE_VIEW_STORAGE_KEY);
    if (!savedViewId || !document.getElementById(savedViewId)) return;

    if (savedViewId === "team-management-view") {
      const sidebarTeamMgmt = document.getElementById(
        "sidebarTeamManagementItem"
      );
      const teamMgmtView = document.getElementById("team-management-view");

      if (teamMgmtView) {
        pageViews.forEach((view) => {
          view.style.display =
            view.id === "team-management-view" ? "block" : "none";
        });

        document
          .querySelectorAll(".sidebar .menu li")
          .forEach((li) => li.classList.remove("active"));

        if (sidebarTeamMgmt) {
          sidebarTeamMgmt.style.display = "block";
          sidebarTeamMgmt.classList.add("active");
        }

        window.scrollTo({ top: 0, behavior: "smooth" });
      }
    } else {
      showView(savedViewId, true);
    }
  } catch (e) {
    console.warn("Could not restore active view from localStorage:", e);
  }
})();

window.handleTournamentClick = function (teamRegId, isUserManager) {
  if (isUserManager) {
    const sidebarTeamMgmt = document.getElementById(
      "sidebarTeamManagementItem"
    );
    if (sidebarTeamMgmt) {
      sidebarTeamMgmt.style.display = "block";
    }

    const teamMgmtView = document.getElementById("team-management-view");
    if (teamMgmtView) {
      document.querySelectorAll(".page-view").forEach((v) => {
        v.style.display = "none";
      });
      teamMgmtView.style.display = "block";

      document
        .querySelectorAll(".sidebar .menu li")
        .forEach((li) => li.classList.remove("active"));
      if (sidebarTeamMgmt) {
        sidebarTeamMgmt.classList.add("active");
      }

      try {
        localStorage.setItem(ACTIVE_VIEW_STORAGE_KEY, "team-management-view");
      } catch (e) {
        console.warn("Could not save active view to localStorage:", e);
      }

      window.scrollTo({ top: 0, behavior: "smooth" });
    }
  } else {
    fetch(`/api/teams/get-roster?teamRegId=${teamRegId}`)
      .then((response) => response.json())
      .then((data) => {
        const container = document.getElementById("playerSquadCardContainer");
        const squadView = document.getElementById("player-squad-view");

        if (container) {
          let membersHtml = "";
          if (data && data.length > 0) {
            data.sort((a, b) => {
              const roleA = (a.roleInTeam || "").toUpperCase();
              const roleB = (b.roleInTeam || "").toUpperCase();

              if (roleA === "CAPTAIN") return -1;
              if (roleB === "CAPTAIN") return 1;
              if (roleA === "VICE_CAPTAIN") return -1;
              if (roleB === "VICE_CAPTAIN") return 1;
              return 0;
            });

            data.forEach((member) => {
              const role = (member.roleInTeam || "PLAYER").toUpperCase();
              let badgeStyle =
                "background: rgba(255,255,255,0.1); color: #9ca3af;";
              if (role === "CAPTAIN") {
                badgeStyle =
                  "background: rgba(133, 77, 14, 0.4); color: #f97316; border: 1px solid rgba(249, 115, 22, 0.2);";
              } else if (role === "VICE_CAPTAIN") {
                badgeStyle =
                  "background: rgba(30, 58, 138, 0.4); color: #60a5fa; border: 1px solid rgba(96, 165, 250, 0.2);";
              }

              const fullName =
                member.playerProfile?.fullName || member.fullName || "Player";
              const initials = fullName
                .split(" ")
                .map((n) => n[0])
                .join("")
                .toUpperCase()
                .substring(0, 2);
              const avatarSrc =
                member.playerProfile?.profileImage || member.profileImage;
              const memberId = member.playerId;

              const avatarHtml = avatarSrc
                ? `<img src="${avatarSrc}" alt="" class="roster-avatar" style="width: 40px; height: 40px; border-radius: 50%; object-fit: cover;" />`
                : `<div class="roster-avatar" style="width: 40px; height: 40px; border-radius: 50%; background: #27272a; color: #f97316; display: flex; align-items: center; justify-content: center; font-weight: 600; font-size: 14px;">${initials}</div>`;

              membersHtml += `
                <li class="roster-item" style="display: flex; align-items: center; justify-content: space-between; padding: 12px 16px; border-bottom: 1px solid rgba(255,255,255,0.05);">
                    <div class="roster-member-info" onclick="openPlayerProfileModal(${memberId})" style="display: flex; align-items: center; gap: 12px; cursor: pointer;">
                        ${avatarHtml}
                        <div>
                            <strong style="color: #fff; display: block; font-size: 14px;">${fullName}</strong>
                            <span class="roster-role-tag" style="font-size: 10px; padding: 2px 10px; border-radius: 20px; font-weight: 600; display: inline-block; margin-top: 4px; ${badgeStyle}">${role}</span>
                        </div>
                    </div>
                </li>`;
            });
          } else {
            membersHtml = `<li class="roster-empty" style="padding: 20px; text-align: center; color: #9ca3af;">No squad members found for this team.</li>`;
          }

          container.innerHTML = `
            <section class="team-management-card" style="margin-bottom: 20px; background: #18181b; border: 1px solid rgba(255,255,255,0.1); border-radius: 16px; padding: 24px;">
                <div class="card-title" style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
                    <div style="display: flex; align-items: center; gap: 10px;">
                        <i class="fa-solid fa-people-group" style="color: #f97316;"></i>
                        <h2 style="color: #fff; font-size: 18px; margin: 0;">Team Squad View</h2>
                    </div>
                    <span class="role-pill" style="background: rgba(59, 130, 246, 0.2); color: #60a5fa; padding: 4px 12px; border-radius: 20px; font-size: 12px; font-weight: 600;">Player View</span>
                </div>
                <div class="roster-block">
                    <h4 style="color: #9ca3af; font-size: 12px; text-transform: uppercase; margin-bottom: 12px;">Team Roster</h4>
                    <ul class="roster-list" style="list-style: none; padding: 0; margin: 0;">
                        ${membersHtml}
                    </ul>
                </div>
            </section>`;
        }

        if (squadView) {
          document
            .querySelectorAll(".page-view")
            .forEach((v) => (v.style.display = "none"));
          squadView.style.display = "block";
          document
            .querySelectorAll(".sidebar .menu li")
            .forEach((li) => li.classList.remove("active"));
          window.scrollTo({ top: 0, behavior: "smooth" });
        }
      })
      .catch((err) => {
        console.error("Error loading squad:", err);
        alert("Failed to load team squad details.");
      });
  }
};

renderProfileView();

// ==========================================
// WHATSAPP-STYLE CROP & ZOOM STATE
// ==========================================
let selectedFileObject = null;
let imgScale = 1;
let imgPosX = 0;
let imgPosY = 0;
let isDragging = false;
let startX = 0,
  startY = 0;

function handleFileSelect(event) {
  const file = event.target.files[0];
  if (!file) return;

  selectedFileObject = file;
  const previewUrl = URL.createObjectURL(file);

  if (previewImg) {
    previewImg.onload = () => {
      imgScale = 1;
      imgPosX = 0;
      imgPosY = 0;
      if (zoomSlider) zoomSlider.value = 1;
      updateImageTransform();
    };
    previewImg.src = previewUrl;
  }

  if (imageModal) {
    imageModal.style.display = "flex";
  }
}

function closeImageModal() {
  if (imageModal) imageModal.style.display = "none";
  if (fileInput) fileInput.value = "";
  selectedFileObject = null;
}

if (cropContainer) {
  cropContainer.addEventListener("mousedown", (e) => {
    isDragging = true;
    startX = e.clientX - imgPosX;
    startY = e.clientY - imgPosY;
  });

  window.addEventListener("mousemove", (e) => {
    if (!isDragging) return;
    imgPosX = e.clientX - startX;
    imgPosY = e.clientY - startY;
    updateImageTransform();
  });

  window.addEventListener("mouseup", () => {
    isDragging = false;
  });
}

function onZoomSliderChange(val) {
  imgScale = parseFloat(val);
  updateImageTransform();
}

function adjustZoom(amount) {
  imgScale = Math.min(Math.max(0.5, imgScale + amount), 3);
  if (zoomSlider) zoomSlider.value = imgScale;
  updateImageTransform();
}

function updateImageTransform() {
  if (previewImg) {
    previewImg.style.transform = `translate(${imgPosX}px, ${imgPosY}px) scale(${imgScale})`;
  }
}

async function uploadConfirmedImage() {
  if (!selectedFileObject) {
    alert("No image selected!");
    return;
  }

  let formData = new FormData();
  formData.append("profileImage", selectedFileObject);

  try {
    let response = await fetch("/api/player/update-profileImage", {
      method: "POST",
      body: formData,
    });

    if (response.ok) {
      window.location.reload();
    } else {
      let errorText = await response.text();
      alert("Failed to upload image: " + errorText);
    }
  } catch (error) {
    console.error("Network Error:", error);
    alert("An error occurred while connecting to the server.");
  }
}

// ==========================================
// TEAM MANAGEMENT & INVITE COPY
// ==========================================
document.addEventListener("DOMContentLoaded", () => {
  const copyInviteBtn = document.getElementById("copyInviteBtn");
  const teamInviteCode = document.getElementById("teamInviteCode");

  if (copyInviteBtn && teamInviteCode) {
    copyInviteBtn.addEventListener("click", async () => {
      const inviteLink =
        teamInviteCode.dataset.inviteLink || teamInviteCode.innerText.trim();

      try {
        await navigator.clipboard.writeText(inviteLink);
        const originalHTML = copyInviteBtn.innerHTML;
        copyInviteBtn.innerHTML = '<i class="fa-solid fa-check"></i> Copied!';
        copyInviteBtn.classList.add("copied");

        setTimeout(() => {
          copyInviteBtn.innerHTML = originalHTML;
          copyInviteBtn.classList.remove("copied");
        }, 2000);
      } catch (err) {
        console.error("Failed to copy invite link:", err);
      }
    });
  }
});

let pendingActionData = null;

function handleRosterRoleChange(selectElement) {
  const action = selectElement.value;
  if (!action || action === "") return;

  const teamRegId = selectElement.getAttribute("data-team-reg-id");
  const rosterId = selectElement.getAttribute("data-member-id");
  const teamName = selectElement.getAttribute("data-team-name");
  const playerName = selectElement.getAttribute("data-member-name");

  selectElement.value = "";
  pendingActionData = { teamRegId, rosterId, teamName, action, playerName };

  const modal = document.getElementById("actionConfirmModal");
  const titleEl = document.getElementById("modalTitle");
  const msgEl = document.getElementById("modalMessage");
  const confirmBtn = document.getElementById("modalConfirmBtn");
  const cancelBtn = document.getElementById("modalCancelBtn");

  if (!modal) return;

  cancelBtn.textContent = "Cancel";
  confirmBtn.textContent = "OK";

  if (action === "ASSIGN_VICE_CAPTAIN") {
    titleEl.textContent = "Assign Vice-Captain";
    msgEl.innerHTML = `Are you really want to make <strong style="color: #ffffff;">${playerName}</strong> as a vice-captain of <strong style="color: #ffffff;">${teamName}</strong> team?`;
    confirmBtn.style.backgroundColor = "#ff7a00";
  } else if (action === "REMOVE_VICE_CAPTAIN") {
    titleEl.textContent = "Remove Vice-Captain";
    msgEl.innerHTML = `Are you really want to remove vice-captain role from <strong style="color: #ffffff;">${playerName}</strong> in <strong style="color: #ffffff;">${teamName}</strong> team?`;
    confirmBtn.style.backgroundColor = "#ff7a00";
  } else if (action === "TRANSFER_CAPTAINCY") {
    titleEl.textContent = "Transfer Captaincy";
    msgEl.innerHTML = `Are you really want to make <strong style="color: #ffffff;">${playerName}</strong> as a captain of <strong style="color: #ffffff;">${teamName}</strong> team?`;
    confirmBtn.style.backgroundColor = "#ff7a00";
  } else if (action === "REMOVE_PLAYER") {
    titleEl.textContent = "Remove Player";
    msgEl.innerHTML = `Are you really want to remove <strong style="color: #ef4444;">${playerName}</strong> from <strong style="color: #ffffff;">${teamName}</strong> team?`;
    confirmBtn.style.backgroundColor = "#ef4444";
  }

  modal.style.display = "flex";
}

document.getElementById("modalCancelBtn")?.addEventListener("click", () => {
  document.getElementById("actionConfirmModal").style.display = "none";
  pendingActionData = null;
});

document.getElementById("modalConfirmBtn")?.addEventListener("click", () => {
  if (!pendingActionData) return;

  const { teamRegId, rosterId, action } = pendingActionData;

  fetch(
    `/api/teams/update-role?teamRegId=${teamRegId}&rosterId=${rosterId}&action=${action}`,
    {
      method: "POST",
    }
  )
    .then((response) => {
      if (response.ok) {
        location.reload();
      } else {
        return response.text().then((err) => {
          alert("Error: " + err);
        });
      }
    })
    .catch((error) => {
      console.error("Error:", error);
    })
    .finally(() => {
      document.getElementById("actionConfirmModal").style.display = "none";
      pendingActionData = null;
    });
});

let currentEditingTeamRegId = null;

function openEditTeamNameModal(teamRegId, currentTeamName) {
  currentEditingTeamRegId = teamRegId;
  const modal = document.getElementById("editTeamNameModal");
  const input = document.getElementById("editTeamNameInput");

  if (input) input.value = currentTeamName;
  if (modal) modal.style.display = "flex";
}

function closeEditTeamNameModal() {
  const modal = document.getElementById("editTeamNameModal");
  if (modal) modal.style.display = "none";
  currentEditingTeamRegId = null;
}

async function submitNewTeamName() {
  const input = document.getElementById("editTeamNameInput");
  const newTeamName = input ? input.value.trim() : "";

  if (!newTeamName) {
    alert("Team name cannot be empty!");
    return;
  }

  try {
    let response = await fetch(
      `/api/teams/update-name?teamRegId=${currentEditingTeamRegId}&newName=${encodeURIComponent(
        newTeamName
      )}`,
      {
        method: "POST",
      }
    );

    if (response.ok) {
      closeEditTeamNameModal();
      document.querySelectorAll(".team-mgmt-identity h3, h2").forEach((el) => {
        if (
          el.innerText.includes("Team Management:") ||
          el.closest("#team-management-view")
        ) {
          if (el.tagName === "H3") {
            el.innerText = newTeamName;
          } else if (el.tagName === "H2") {
            el.innerText = "Team Management: " + newTeamName;
          }
        }
      });

      const teamMgmtView = document.getElementById("team-management-view");
      const sidebarTeamMgmt = document.getElementById(
        "sidebarTeamManagementItem"
      );

      if (teamMgmtView) {
        document.querySelectorAll(".page-view").forEach((v) => {
          v.style.display = "none";
        });
        teamMgmtView.style.display = "block";

        document
          .querySelectorAll(".sidebar .menu li")
          .forEach((li) => li.classList.remove("active"));

        if (sidebarTeamMgmt) {
          sidebarTeamMgmt.style.display = "block";
          sidebarTeamMgmt.classList.add("active");
        }

        try {
          localStorage.setItem(ACTIVE_VIEW_STORAGE_KEY, "team-management-view");
        } catch (e) {
          console.warn("Could not save active view to localStorage:", e);
        }

        window.scrollTo({ top: 0, behavior: "smooth" });
      }
    } else {
      let errText = await response.text();
      alert("Failed to update team name: " + errText);
    }
  } catch (error) {
    console.error("Error updating team name:", error);
    alert("An error occurred while connecting to the server.");
  }
}

async function saveField(field) {
  const inputEl = document.getElementById("edit" + capitalize(field));
  const newValue = inputEl ? inputEl.value : "";

  try {
    let response = await fetch(
      `/api/player/update-field?field=${field}&value=${encodeURIComponent(
        newValue
      )}`,
      {
        method: "POST",
      }
    );

    if (response.ok) {
      playerProfileData[field] = newValue;

      const viewEl = document.getElementById("view" + capitalize(field));
      if (viewEl) {
        viewEl.innerText =
          field === "dob"
            ? formatDobDisplay(newValue)
            : field === "phone"
            ? maskPhone(newValue)
            : newValue;
      }

      if (field === "sport") {
        const profileSport = document.getElementById("profileSport");
        if (profileSport) profileSport.innerText = newValue + " Player";
      }

      if (field === "email") {
        const linkedGoogleEmail = document.getElementById("linkedGoogleEmail");
        if (linkedGoogleEmail) linkedGoogleEmail.innerText = newValue;
      }

      exitFieldEditMode(field);
    } else {
      let errText = await response.text();
      alert("Failed to update profile field: " + errText);
    }
  } catch (error) {
    console.error("Error saving profile field:", error);
    alert("An error occurred while connecting to the server.");
  }
}

// ==========================================
// ID PROOF EDIT MODAL HANDLERS
// ==========================================
function openEditIdModal() {
  const modal = document.getElementById("editIdModal");
  if (modal) modal.style.display = "flex";
}

function closeEditIdModal() {
  const modal = document.getElementById("editIdModal");
  if (modal) modal.style.display = "none";
}

function updateFileName(input) {
  const displaySpan = document.getElementById("updateFileNameDisplay");
  if (input.files && input.files[0]) {
    displaySpan.textContent = input.files[0].name;
    displaySpan.style.color = "#22c55e";
  } else {
    displaySpan.textContent = "No file chosen";
    displaySpan.style.color = "#71717a";
  }
}

function submitUpdateIdProof(event) {
  event.preventDefault();

  const form = document.getElementById("updateIdForm");
  const formData = new FormData(form);

  const fileInput = document.getElementById("updateIdDocument");
  if (!fileInput || fileInput.files.length === 0) {
    Swal.fire({
      title: "File Required",
      text: "Please select a document file before updating.",
      icon: "warning",
      background: "#18181b",
      color: "#ffffff",
      confirmButtonColor: "#ff6b00",
    });
    return;
  }

  fetch(`/api/player/update-id-proof`, {
    method: "POST",
    body: formData,
  })
    .then((response) => {
      return response.text().then((text) => {
        try {
          const data = JSON.parse(text);
          if (!response.ok)
            throw new Error(data.message || "Failed to update ID proof.");
          return data;
        } catch (e) {
          if (!response.ok) throw new Error(text || "Server error occurred.");
          return { success: true };
        }
      });
    })
    .then(() => {
      closeEditIdModal();
      Swal.fire({
        title: "Updated Successfully!",
        text: "Your ID proof has been updated.",
        icon: "success",
        background: "#18181b",
        color: "#ffffff",
        confirmButtonColor: "#ff6b00",
      }).then(() => {
        location.reload();
      });
    })
    .catch((error) => {
      console.error("Error:", error);
      Swal.fire({
        title: "Update Failed",
        text: error.message || "Could not update ID proof.",
        icon: "error",
        background: "#18181b",
        color: "#ffffff",
        confirmButtonColor: "#ff6b00",
      });
    });
}

// ==========================================
// PLAYER PROFILE QUICK VIEW MODAL HANDLERS (Global scope attached)
// ==========================================
window.openPlayerProfileModal = function (playerId) {
  if (!playerId) return;

  fetch(`/api/organizer/player/${playerId}/details`)
    .then((response) => {
      if (!response.ok) throw new Error("Failed to fetch player details");
      return response.json();
    })
    .then((profile) => {
      document.getElementById("modalPlayerAvatar").src =
        profile.profileImage ||
        "https://ui-avatars.com/api/?name=Player&background=151518&color=ff914d";
      document.getElementById("modalPlayerName").innerText =
        profile.fullName || "Unknown Player";
      document.getElementById("modalPlayerUsername").innerText =
        "@" + (profile.username || "player");
      document.getElementById("modalPlayerSport").innerText =
        profile.primarySport || "Not specified";
      document.getElementById("modalPlayerSkill").innerText =
        profile.skillLevel || "Not specified";
      document.getElementById("modalPlayerRole").innerText =
        profile.playerRole || "Not specified";
      document.getElementById("modalPlayerExp").innerText =
        (profile.yearsOfExperience || 0) + " Years";

      document.getElementById("playerProfileModal").style.display = "flex";
    })
    .catch((error) => {
      console.error("Error loading player profile:", error);
    });
};

window.closePlayerProfileModal = function () {
  const modal = document.getElementById("playerProfileModal");
  if (modal) modal.style.display = "none";
};
