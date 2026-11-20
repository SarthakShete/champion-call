document.addEventListener("DOMContentLoaded", () => {
  if (window.organizerProfile != null) {
    const modal = document.getElementById("onboardingModal");
    if (modal) {
      modal.style.display = "none";
      modal.classList.remove("show");
      modal.remove(); // Completely deletes it from the DOM
    }
  }
  let currentSelectedTeamId = null;

  try {
    // CHANGED: the .tab buttons (Overview / Participants / Brackets /
    // Settings) were removed from the HTML. Tab switching is now driven
    // entirely by the sidebar links (.sidebar-link[data-tab]) instead.
    const panels = document.querySelectorAll(".tab-panel");
    const sidebarLinks = document.querySelectorAll(".sidebar-link[data-tab]");
    const appBody = document.body;

    function switchTab(tabName) {
      panels.forEach((p) => p.classList.remove("active"));

      let target = document.getElementById("tab-" + tabName);

      if (!target) {
        tabName = "overview";
        target = document.getElementById("tab-overview");
      }

      if (target) target.classList.add("active");

      document
        .querySelectorAll(".sidebar-link")
        .forEach((l) => l.classList.remove("active"));
      const matchingSidebarLink = document.querySelector(
        `.sidebar-link[data-tab="${tabName}"]`
      );
      if (matchingSidebarLink) matchingSidebarLink.classList.add("active");

      if (tabName === "participants") {
        appBody.classList.add("hide-top-section");
      } else {
        appBody.classList.remove("hide-top-section");
      }

      localStorage.setItem("manageTournamentActiveTab", tabName);
      localStorage.setItem("manageTournamentActiveTabTimestamp", Date.now());
    }

    // Expose for other try/catch blocks further down (e.g. after a
    // successful manual-fixtures save) without re-declaring it.
    window.switchTab = switchTab;

    const SESSION_TIMEOUT_MS = 30 * 60 * 1000;

    const savedTab = localStorage.getItem("manageTournamentActiveTab");
    const savedTimestamp = localStorage.getItem(
      "manageTournamentActiveTabTimestamp"
    );
    // CHANGED: valid tab names now come from the sidebar links (data-tab),
    // since the old .tab button list no longer exists.
    const validTabNames = Array.from(sidebarLinks).map((l) => l.dataset.tab);

    const isRecent =
      savedTimestamp &&
      Date.now() - Number(savedTimestamp) < SESSION_TIMEOUT_MS;

    if (savedTab && validTabNames.includes(savedTab) && isRecent) {
      switchTab(savedTab);
    } else {
      switchTab("overview");
    }

    // CHANGED: sidebar links now call switchTab() directly instead of
    // trying to click a (now nonexistent) .tab button.
    sidebarLinks.forEach((link) => {
      link.addEventListener("click", (e) => {
        e.preventDefault();
        switchTab(link.dataset.tab);
      });
    });
  } catch (err) {
    // Even if tab init itself hits an unexpected error, log it loudly
    // instead of silently leaving the page stuck — check the console
    // for this message if the sidebar/panel mismatch ever comes back.
    console.error("Tab switching failed to initialize:", err);
  }

  // ============================================================
  // EVERYTHING BELOW THIS POINT is wrapped in its own try/catch so
  // that a bug here (missing element, bad selector, etc.) can never
  // again take down tab switching above it.
  // ============================================================
  try {
    (function injectChampionCallLogo() {
      const navbarLeft = document.querySelector(".navbar-left");
      const toggleBtn = document.querySelector(".toggle-btn");
      const sidebar = document.querySelector(".sidebar");

      if (!navbarLeft || !toggleBtn) return;
      if (navbarLeft.querySelector(".cc-nav-logo")) return;

      const logo = document.createElement("div");
      logo.className = "cc-nav-logo";
      logo.innerHTML = `
      <i class="fa-solid fa-trophy"></i>
      <span class="cc-nav-logo-text">CHAMPION<span class="cc-nav-logo-accent">CALL</span></span>
    `;

      navbarLeft.insertBefore(logo, toggleBtn);

      function syncLogoVisibility() {
        const isCollapsed = sidebar && sidebar.classList.contains("collapsed");
        logo.classList.toggle("cc-nav-logo-hidden", isCollapsed);
      }

      syncLogoVisibility();

      if (sidebar) {
        const observer = new MutationObserver(syncLogoVisibility);
        observer.observe(sidebar, {
          attributes: true,
          attributeFilter: ["class"],
        });
      }
    })();

    const settingsForm = document.getElementById("settings-form");
    if (settingsForm) {
      settingsForm.addEventListener("submit", (e) => {
        e.preventDefault();
        alert(
          "Tournament settings saved (wire this up to your backend endpoint)."
        );
      });
    }

    document.querySelectorAll(".btn-mini-delete").forEach((btn) => {
      btn.addEventListener("click", () => {
        const card = btn.closest(".fixture-card");
        if (card && confirm("Delete this fixture?")) {
          card.remove();
        }
      });
    });

    document.querySelectorAll(".btn-mini-edit").forEach((btn) => {
      btn.addEventListener("click", () => {
        alert("Open edit-fixture modal here.");
      });
    });

    // ===== Fixtures tab: Round-rail cards (Edit Fixtures / edit a single match) =====
    const editFixturesBtn = document.getElementById("editFixturesBtn");
    if (editFixturesBtn) {
      editFixturesBtn.addEventListener("click", () => {
        alert("Switch to manual editor mode.");
      });
    }

    document.querySelectorAll(".edit-match-btn").forEach((btn) => {
      btn.addEventListener("click", () => {
        const fixtureId = btn.getAttribute("data-fixture-id");
        alert("Open edit-fixture modal here for fixture #" + fixtureId);
      });
    });

    const allGenerateBtns = document.querySelectorAll(
      "#generateFixturesBtn, #generate-fixtures-btn"
    );
    allGenerateBtns.forEach((btn) => {
      btn.addEventListener("click", (e) => {
        e.preventDefault();
        openChoiceModal();
      });
    });

    // ===== Manual Fixture Builder: show/hide panel =====
    const manualCreationBtn = document.getElementById("manual-creation-btn");
    const preGenerationPanel = document.getElementById("preGenerationPanel");
    const manualMatchCreationPanel = document.getElementById(
      "manualMatchCreationPanel"
    );
    const cancelBuilderBtn = document.getElementById("cancelBuilderBtn");
    const saveAllFixturesBtn = document.getElementById("saveAllFixturesBtn");

    const fixturesTwoCol = document.getElementById("fixturesTwoCol");

    window.showManualBuilder = function () {
      if (preGenerationPanel) preGenerationPanel.style.display = "none";
      if (manualMatchCreationPanel)
        manualMatchCreationPanel.style.display = "block";
      if (fixturesTwoCol) fixturesTwoCol.classList.add("manual-builder-mode");
    };

    window.hideManualBuilder = function () {
      if (manualMatchCreationPanel)
        manualMatchCreationPanel.style.display = "none";
      if (preGenerationPanel) preGenerationPanel.style.display = "block";
      if (fixturesTwoCol)
        fixturesTwoCol.classList.remove("manual-builder-mode");
    };

    if (manualCreationBtn) {
      manualCreationBtn.addEventListener("click", (e) => {
        e.preventDefault();
        showManualBuilder();
      });
    }

    if (cancelBuilderBtn) {
      cancelBuilderBtn.addEventListener("click", () => {
        hideManualBuilder();
      });
    }

    if (saveAllFixturesBtn) {
      saveAllFixturesBtn.addEventListener("click", async () => {
        const tournamentId = getTournamentId();
        if (!tournamentId) return;

        const rows = document.querySelectorAll(
          "#matchRowsWrapper .match-slot-row"
        );
        const payload = [];

        for (const row of rows) {
          const round = row.querySelector(".builder-select").value;
          const zones = row.querySelectorAll(".drop-zone");
          const teamAId = zones[0].dataset.teamId || null;
          const teamBId = zones[1].dataset.teamId || null;

          // Read the combined datetime-local input (e.g., "2026-08-20T18:00")
          const dateTimeInput = row.querySelector(".builder-input");
          const dateTimeValue = dateTimeInput ? dateTimeInput.value : "";

          // Automatically split datetime-local into date and time
          let matchDate = null;
          let matchTime = null;

          if (dateTimeValue && dateTimeValue.includes("T")) {
            const parts = dateTimeValue.split("T");
            matchDate = parts[0]; // Extracts "2026-08-20"
            matchTime = parts[1]; // Extracts "18:00"
          } else {
            matchDate = dateTimeValue;
          }

          // Read the venue input
          const venueInput = row.querySelector(".venue-input");
          const venue = venueInput ? venueInput.value.trim() : "";

          if (!teamAId || !teamBId) {
            Swal.fire({
              title: "Incomplete Match",
              text: "Every match slot needs both Team A and Team B filled in before saving.",
              icon: "warning",
              confirmButtonColor: "#ff6b00",
              background: "#1a1a1a",
              color: "#fff",
            });
            return;
          }

          if (!venue) {
            Swal.fire({
              title: "Missing Venue",
              text: "Every match slot needs a venue before saving.",
              icon: "warning",
              confirmButtonColor: "#ff6b00",
              background: "#1a1a1a",
              color: "#fff",
            });
            return;
          }

          payload.push({
            roundNumber: round,
            matchDate: matchDate, // Uses your extracted variable
            matchTime: matchTime, // Uses your extracted variable
            venueLocation: venue, // Uses your extracted variable
            teamAId: teamAId,
            teamBId: teamBId,
            status: "SCHEDULED",
          });
        }

        try {
          const response = await fetch(
            `/api/organizer/tournament/${tournamentId}/manual-fixtures`,
            {
              method: "POST",
              headers: { "Content-Type": "application/json" },
              body: JSON.stringify({ fixtures: payload }),
            }
          );

          if (!response.ok) {
            const errorMsg = await response.text();
            throw new Error(errorMsg || "Failed to save fixtures.");
          }

          Swal.fire({
            title: "🎉 Saved!",
            text: "Fixtures created successfully!",
            icon: "success",
            confirmButtonColor: "#2ecc71",
            background: "#1a1a1a",
            color: "#fff",
          }).then(() => {
            window.location.reload();
          });
        } catch (error) {
          Swal.fire({
            title: "Error!",
            text: error.message,
            icon: "error",
            confirmButtonColor: "#ff6b00",
            background: "#1a1a1a",
            color: "#fff",
          });
        }
      });
    }

    document.querySelectorAll(".btn-danger").forEach((btn) => {
      btn.addEventListener("click", () => {
        const action = btn.textContent.trim();
        if (
          confirm(
            `Are you sure you want to: ${action}? This action cannot be undone.`
          )
        ) {
          alert(`${action} confirmed. Wire this to your backend.`);
        }
      });
    });

    const teamDrawerOverlay = document.getElementById("teamDrawerOverlay");
    const teamSlideDrawer = document.getElementById("teamSlideDrawer");
    const drawerCloseBtn = document.getElementById("drawerCloseBtn");
    const drawerTeamName = document.getElementById("drawerTeamName");
    const drawerApproveBtn = document.getElementById("drawerApproveBtn");
    const drawerRejectBtn = document.getElementById("drawerRejectBtn");

    document.body.addEventListener("click", (e) => {
      const teamCell = e.target.closest(".clickable-team");
      if (!teamCell) return;

      const teamName = teamCell.textContent.trim();
      const teamId = teamCell.getAttribute("data-team-id");

      currentSelectedTeamId = teamId;

      if (!teamId || teamId === "null") {
        console.error(
          "Missing 'data-team-id' attribute on the clicked row!",
          teamCell
        );
        alert(
          "Error: Team ID is missing in the HTML! Please check your Thymeleaf template."
        );
        return;
      }

      if (drawerTeamName) drawerTeamName.textContent = teamName;

      if (teamSlideDrawer) teamSlideDrawer.classList.add("active");
      if (teamDrawerOverlay) teamDrawerOverlay.classList.add("active");

      const squadListContainer = document.getElementById("drawerSquadList");
      const teamStatusEl = document.getElementById("drawerTeamStatus");

      if (squadListContainer) {
        squadListContainer.innerHTML =
          '<li class="squad-member-row">Loading roster...</li>';
      }

      fetch(`/api/organizer/tournament/team/${teamId}/players`)
        .then((response) => {
          if (!response.ok) {
            throw new Error("Failed to fetch team players");
          }
          return response.json();
        })
        .then((data) => {
          if (teamStatusEl && data.status) {
            let statusClass = "status-pending";
            let statusText = data.status;

            if (data.status === "APPROVED" || data.status === "CONFIRMED") {
              statusClass = "status-approved";
              statusText = "✓ Approved";
            } else if (data.status === "REJECTED") {
              statusClass = "status-rejected";
              statusText = "✕ Rejected";
            } else {
              statusText = "⏱ " + data.status;
            }

            teamStatusEl.innerHTML = `<span class="status-pill ${statusClass}" style="padding: 4px 10px; border-radius: 20px; font-weight: 600; font-size: 12px; display: inline-block; ${
              statusClass === "status-approved"
                ? "background: rgba(46, 204, 113, 0.2); color: #2ecc71;"
                : statusClass === "status-rejected"
                ? "background: rgba(231, 76, 60, 0.2); color: #e74c3c;"
                : "background: rgba(243, 156, 18, 0.2); color: #f39c12;"
            }">${statusText}</span>`;
          }

          if (squadListContainer && data.roster) {
            squadListContainer.innerHTML = "";

            if (data.roster.length === 0) {
              squadListContainer.innerHTML =
                '<li class="squad-member-row">No players added yet.</li>';
              return;
            }

            data.roster.sort((a, b) => {
              const getPriority = (role) => {
                if (role === "CAPTAIN") return 1;
                if (role === "VICE_CAPTAIN") return 2;
                return 3;
              };
              return getPriority(a.roleInTeam) - getPriority(b.roleInTeam);
            });

            data.roster.forEach((player) => {
              const playerName =
                player.playerProfile && player.playerProfile.fullName
                  ? player.playerProfile.fullName
                  : "Unknown Player";

              const playerId =
                player.playerProfile && player.playerProfile.id
                  ? player.playerProfile.id
                  : "";

              let roleLabel = "";
              if (player.roleInTeam === "CAPTAIN") roleLabel = " (Captain)";
              else if (player.roleInTeam === "VICE_CAPTAIN")
                roleLabel = " (Vice-Captain)";

              const li = document.createElement("li");
              li.className = "squad-member-row";
              li.innerHTML = `
              <span class="player-name clickable-player" data-player-id="${playerId}" style="cursor: pointer; color: #ff914d; font-weight: 500;">
                ${playerName} <span style="color: #a1a1aa; font-size: 11px;">${roleLabel}</span>
              </span>
              <button
                class="btn-mini btn-view-id"
                data-player="${playerName}"
                data-doc="${
                  player.idProofUrl ||
                  (player.playerProfile
                    ? player.playerProfile.playerIdProof
                    : "") ||
                  ""
                }"
              >
                View ID
              </button>
            `;
              squadListContainer.appendChild(li);
            });
          }
        })
        .catch((error) => {
          console.error("Error loading players data:", error);
          if (squadListContainer) {
            squadListContainer.innerHTML =
              '<li class="squad-member-row" style="color: #ff914d;">Failed to load players.</li>';
          }
        });
    });

    function closeTeamDrawer() {
      if (teamSlideDrawer) teamSlideDrawer.classList.remove("active");
      if (teamDrawerOverlay) teamDrawerOverlay.classList.remove("active");
    }

    if (drawerCloseBtn)
      drawerCloseBtn.addEventListener("click", closeTeamDrawer);
    if (teamDrawerOverlay)
      teamDrawerOverlay.addEventListener("click", closeTeamDrawer);

    document.body.addEventListener("click", (e) => {
      const playerSpan = e.target.closest(".clickable-player");
      if (!playerSpan) return;

      const playerId = playerSpan.getAttribute("data-player-id");
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
    });

    window.closePlayerProfileModal = function () {
      const modal = document.getElementById("playerProfileModal");
      if (modal) modal.style.display = "none";
    };

    if (drawerApproveBtn) {
      drawerApproveBtn.addEventListener("click", () => {
        const teamName = drawerTeamName
          ? drawerTeamName.textContent.trim()
          : "this team";

        Swal.fire({
          title: "Approve Team?",
          text: `Are you sure you want to approve "${teamName}"?`,
          icon: "warning",
          showCancelButton: true,
          confirmButtonColor: "#ff6b00",
          cancelButtonColor: "#333",
          confirmButtonText: "Yes, Approve",
          cancelButtonText: "Cancel",
          background: "#1a1a1a",
          color: "#fff",
        }).then((result) => {
          if (result.isConfirmed) {
            fetch(
              `/api/organizer/tournament/team/${currentSelectedTeamId}/approve`,
              {
                method: "POST",
                headers: { "Content-Type": "application/json" },
              }
            )
              .then((response) => {
                if (!response.ok) throw new Error("Failed to approve team");
                return response.text();
              })
              .then(() => {
                window.location.reload();
              })
              .catch((error) => {
                console.error("Error approving team:", error);
                Swal.fire({
                  title: "Error!",
                  text: "Failed to approve team. Please try again.",
                  icon: "error",
                  confirmButtonColor: "#ff6b00",
                  background: "#1a1a1a",
                  color: "#fff",
                });
              });
          }
        });
      });
    }

    if (drawerRejectBtn) {
      drawerRejectBtn.addEventListener("click", () => {
        const teamName = drawerTeamName
          ? drawerTeamName.textContent.trim()
          : "this team";

        Swal.fire({
          title: "Reject Team?",
          text: `Are you really sure you want to reject "${teamName}"?`,
          icon: "warning",
          showCancelButton: true,
          confirmButtonColor: "#e74c3c",
          cancelButtonColor: "#333",
          confirmButtonText: "Yes, Reject",
          cancelButtonText: "Cancel",
          background: "#1a1a1a",
          color: "#fff",
        }).then((result) => {
          if (result.isConfirmed) {
            fetch(
              `/api/organizer/tournament/team/${currentSelectedTeamId}/reject`,
              {
                method: "POST",
                headers: { "Content-Type": "application/json" },
              }
            )
              .then((response) => {
                if (!response.ok) throw new Error("Failed to reject team");
                return response.text();
              })
              .then(() => {
                window.location.reload();
              })
              .catch((error) => {
                console.error("Error rejecting team:", error);
                Swal.fire({
                  title: "Error!",
                  text: "Failed to reject team. Please try again.",
                  icon: "error",
                  confirmButtonColor: "#ff6b00",
                  background: "#1a1a1a",
                  color: "#fff",
                });
              });
          }
        });
      });
    }

    const docModalOverlay = document.getElementById("docModalOverlay");
    const docModalCloseBtn = document.getElementById("docModalCloseBtn");
    const docModalTitle = document.getElementById("docModalTitle");
    const docPreviewImage = document.getElementById("docPreviewImage");

    document.body.addEventListener("click", (e) => {
      const btn = e.target.closest(".btn-view-id");
      if (!btn) return;

      e.stopPropagation();
      const playerName = btn.getAttribute("data-player");
      const docUrl = btn.getAttribute("data-doc");

      if (docModalTitle) docModalTitle.textContent = `${playerName} - ID Proof`;
      if (docPreviewImage) docPreviewImage.src = docUrl;

      if (docModalOverlay) docModalOverlay.classList.add("active");
    });

    document.querySelectorAll(".btn-view-receipt").forEach((btn) => {
      btn.addEventListener("click", (e) => {
        e.stopPropagation();
        const receiptUrl = btn.getAttribute("data-receipt");

        if (docModalTitle)
          docModalTitle.textContent = "Payment Entry Fee Receipt";
        if (docPreviewImage) docPreviewImage.src = receiptUrl;

        if (docModalOverlay) docModalOverlay.classList.add("active");
      });
    });

    function closeDocModal() {
      if (docModalOverlay) docModalOverlay.classList.remove("active");
    }

    if (docModalCloseBtn)
      docModalCloseBtn.addEventListener("click", closeDocModal);
    if (docModalOverlay) {
      docModalOverlay.addEventListener("click", (e) => {
        if (e.target === docModalOverlay) closeDocModal();
      });
    }

    const filterDropdownBtn = document.getElementById("filterDropdownBtn");
    const filterMenu = document.getElementById("filterMenu");

    if (filterDropdownBtn && filterMenu) {
      filterDropdownBtn.addEventListener("click", (e) => {
        e.stopPropagation();
        filterMenu.classList.toggle("active");
      });

      document.addEventListener("click", (e) => {
        if (
          !filterDropdownBtn.contains(e.target) &&
          !filterMenu.contains(e.target)
        ) {
          filterMenu.classList.remove("active");
        }
      });
    }

    const searchInput = document.querySelector(
      '.search-box input[type="text"]'
    );
    const filterOptions = document.querySelectorAll(".filter-option");
    let currentStatusFilter = "ALL";

    function applyTableFilters() {
      const keyword = searchInput ? searchInput.value.toLowerCase().trim() : "";
      const teamRows = document.querySelectorAll(".team-row");
      const noTeamsMessageRow = document.getElementById("noTeamsMessageRow");
      let visibleCount = 0;

      teamRows.forEach((row) => {
        const teamNameEl = row.querySelector(".name-cell");
        const statusPill = row.querySelector(".status-pill");

        if (!teamNameEl) return;

        const teamNameText = teamNameEl.textContent.toLowerCase().trim();
        const statusText = statusPill
          ? statusPill.textContent.toLowerCase().trim()
          : "";

        const matchesSearch = teamNameText.includes(keyword);

        let matchesStatus = false;
        if (currentStatusFilter === "ALL") {
          matchesStatus = true;
        } else if (
          currentStatusFilter === "PENDING" &&
          statusText.includes("pending")
        ) {
          matchesStatus = true;
        } else if (
          currentStatusFilter === "APPROVED" &&
          (statusText.includes("approved") || statusText.includes("confirmed"))
        ) {
          matchesStatus = true;
        } else if (
          currentStatusFilter === "REJECTED" &&
          statusText.includes("rejected")
        ) {
          matchesStatus = true;
        }

        if (matchesSearch && matchesStatus) {
          row.style.display = "";
          visibleCount++;
        } else {
          row.style.display = "none";
        }
      });

      if (noTeamsMessageRow) {
        noTeamsMessageRow.style.display = visibleCount === 0 ? "" : "none";
      }
    }

    if (searchInput) {
      searchInput.addEventListener("input", applyTableFilters);
    }

    filterOptions.forEach((option) => {
      option.addEventListener("click", (e) => {
        e.preventDefault();
        currentStatusFilter = option.getAttribute("data-status").toUpperCase();
        applyTableFilters();
        if (filterMenu) filterMenu.classList.remove("active");
      });
    });

    applyTableFilters();
  } catch (err) {
    // Logged so the rest of the app (drawers, filters, buttons) failing
    // never again silently masks the fact that tab switching above it
    // is a separate, independent block that keeps working regardless.
    console.error("Page setup encountered an error:", err);
  }
});

function getTournamentId() {
  const inputField = document.getElementById("tournamentId");
  if (inputField && inputField.value) return inputField.value;

  const pathSegments = window.location.pathname.split("/");
  const index = pathSegments.indexOf("tournaments");
  if (index !== -1 && pathSegments[index + 1]) {
    return pathSegments[index + 1];
  }
  console.error("Tournament ID could not be found!");
  return null;
}

function openChoiceModal() {
  // CHANGED: this used to offer a two-way choice (Automatic Generation vs
  // Manual Creation). Manual Creation now has its own dedicated button
  // (#manual-creation-btn -> showManualBuilder()), so this modal is just
  // a plain Cancel/OK confirmation that goes straight into automatic
  // fixture generation.
  Swal.fire({
    title: "⚠️ Generate Fixtures?",
    text: "This will securely shuffle the approved teams and build your match schedules automatically.",
    icon: "warning",
    showCancelButton: true,
    confirmButtonText: "OK",
    cancelButtonText: "Cancel",
    confirmButtonColor: "#ff914d",
    cancelButtonColor: "#27272a",
    background: "#18181c",
    color: "#fff",
  }).then((result) => {
    if (result.isConfirmed) {
      executeAutomaticGeneration();
    }
  });
}

async function executeAutomaticGeneration() {
  const tournamentId = getTournamentId();
  if (!tournamentId) {
    Swal.fire({
      title: "Error",
      text: "Tournament ID is missing.",
      icon: "error",
      confirmButtonColor: "#ff6b00",
      background: "#1a1a1a",
      color: "#fff",
    });
    return;
  }

  const loadingOverlay = document.getElementById("loadingOverlay");
  if (loadingOverlay) loadingOverlay.style.display = "flex";

  try {
    const response = await fetch(
      `/api/organizer/tournament/${tournamentId}/generate-fixtures`,
      {
        method: "POST",
        headers: { "Content-Type": "application/json" },
      }
    );

    if (loadingOverlay) loadingOverlay.style.display = "none";

    if (!response.ok) {
      const errorMsg = await response.text();
      throw new Error(errorMsg || "Failed to generate fixtures.");
    }

    Swal.fire({
      title: "🎉 Success!",
      text: "Fixtures generated successfully!",
      icon: "success",
      confirmButtonColor: "#2ecc71",
      background: "#1a1a1a",
      color: "#fff",
      confirmButtonText: "OK",
    }).then(() => {
      window.location.reload();
    });
  } catch (error) {
    if (loadingOverlay) loadingOverlay.style.display = "none";
    Swal.fire({
      title: "Error!",
      text: error.message,
      icon: "error",
      confirmButtonColor: "#ff6b00",
      background: "#1a1a1a",
      color: "#fff",
    });
  }
}

// ============================================================
// MANUAL FIXTURE BUILDER — drag-and-drop team assignment
// ============================================================

function dragTeam(event) {
  const chip = event.currentTarget;
  event.dataTransfer.setData(
    "text/plain",
    JSON.stringify({
      id: chip.dataset.teamId,
      name: chip.dataset.teamName,
      logo: chip.dataset.teamLogo,
    })
  );
}

function allowDrop(event) {
  event.preventDefault();
  event.currentTarget.classList.add("drag-over");
}

function removeDropStyle(event) {
  event.currentTarget.classList.remove("drag-over");
}

function dropTeam(event, dropZone) {
  event.preventDefault();
  dropZone.classList.remove("drag-over");

  const row = dropZone.closest(".match-slot-row");
  const undoBtn = row.querySelector(".btn-icon-action");

  const previousState = {
    zone: dropZone,
    hadTeam: dropZone.classList.contains("filled"),
    teamId: dropZone.dataset.teamId || null,
    teamName: dropZone.dataset.teamName || null,
    teamLogo: dropZone.dataset.teamLogo || null,
    html: dropZone.innerHTML,
  };

  if (previousState.hadTeam) {
    returnTeamToPool(
      previousState.teamId,
      previousState.teamName,
      previousState.teamLogo
    );
  }

  try {
    const teamData = JSON.parse(event.dataTransfer.getData("text/plain"));

    dropZone.dataset.teamId = teamData.id;
    dropZone.dataset.teamName = teamData.name;
    dropZone.dataset.teamLogo = teamData.logo;

    dropZone.classList.add("filled");
    dropZone.innerHTML = `
                <div class="drop-zone-filled-content">
                    <img src="${teamData.logo}" style="width:20px; height:20px; border-radius:50%; object-fit:cover; flex-shrink:0;">
                    <span class="drop-zone-team-name" title="${teamData.name}">${teamData.name}</span>
                </div>
                <input type="hidden" name="teamId" value="${teamData.id}">
                <button type="button" class="drop-zone-remove-btn" onclick="clearDropZone(this)" title="Remove Team">&times;</button>
            `;

    const chipToRemove = document.querySelector(
      `.approved-team-chip[data-team-id="${teamData.id}"]`
    );
    if (chipToRemove) chipToRemove.remove();

    checkEmptyPool();

    row.dataset.lastAction = JSON.stringify({
      type: "DROP",
      zoneSelector:
        dropZone === row.querySelectorAll(".drop-zone")[0] ? "A" : "B",
      teamId: teamData.id,
      teamName: teamData.name,
      teamLogo: teamData.logo,
      previousState: previousState,
    });
    undoBtn.removeAttribute("disabled");
  } catch (e) {
    console.error("Invalid drop data");
  }
}

function undoLastAction(undoBtn) {
  const row = undoBtn.closest(".match-slot-row");
  if (!row.dataset.lastAction) return;

  const action = JSON.parse(row.dataset.lastAction);
  const dropZones = row.querySelectorAll(".drop-zone");
  const targetZone = action.zoneSelector === "A" ? dropZones[0] : dropZones[1];

  if (action.type === "DROP") {
    if (targetZone.dataset.teamId) {
      returnTeamToPool(
        targetZone.dataset.teamId,
        targetZone.dataset.teamName,
        targetZone.dataset.teamLogo
      );
    }

    if (action.previousState.hadTeam) {
      targetZone.dataset.teamId = action.previousState.teamId;
      targetZone.dataset.teamName = action.previousState.teamName;
      targetZone.dataset.teamLogo = action.previousState.teamLogo;
      targetZone.classList.add("filled");
      targetZone.innerHTML = action.previousState.html;

      const chip = document.querySelector(
        `.approved-team-chip[data-team-id="${action.previousState.teamId}"]`
      );
      if (chip) chip.remove();
    } else {
      targetZone.classList.remove("filled");
      targetZone.removeAttribute("data-team-id");
      targetZone.removeAttribute("data-team-name");
      targetZone.removeAttribute("data-team-logo");
      targetZone.innerHTML =
        action.zoneSelector === "A"
          ? "<span>Drop Team A here</span>"
          : "<span>Drop Team B here</span>";
    }
  }

  row.removeAttribute("data-last-action");
  undoBtn.setAttribute("disabled", "true");
  checkEmptyPool();
}

function clearDropZone(btn) {
  const dropZone = btn.closest(".drop-zone");
  const row = dropZone.closest(".match-slot-row");
  const teamId = dropZone.dataset.teamId;
  const teamName = dropZone.dataset.teamName;
  const teamLogo = dropZone.dataset.teamLogo;

  if (teamId) {
    returnTeamToPool(teamId, teamName, teamLogo);
  }

  dropZone.classList.remove("filled");
  dropZone.removeAttribute("data-team-id");
  dropZone.removeAttribute("data-team-name");
  dropZone.removeAttribute("data-team-logo");
  dropZone.innerHTML =
    dropZone.parentElement.querySelectorAll(".drop-zone")[0] === dropZone
      ? "<span>Drop Team A here</span>"
      : "<span>Drop Team B here</span>";

  row.removeAttribute("data-last-action");
  row.querySelector(".btn-icon-action").setAttribute("disabled", "true");
}

function returnTeamToPool(id, name, logo) {
  const pool = document.getElementById("approvedTeamsPool");
  const emptyMsg = pool.querySelector(".empty-pool-msg");
  if (emptyMsg) emptyMsg.remove();

  const newChip = document.createElement("div");
  newChip.className = "approved-team-chip";
  newChip.draggable = true;
  newChip.setAttribute("ondragstart", "dragTeam(event)");
  newChip.dataset.teamId = id;
  newChip.dataset.teamName = name;
  newChip.dataset.teamLogo = logo;
  newChip.innerHTML = `
            <img src="${logo}" alt="Logo" class="team-chip-avatar">
            <span class="team-chip-name">${name}</span>
        `;
  pool.appendChild(newChip);
}

function checkEmptyPool() {
  const pool = document.getElementById("approvedTeamsPool");
  if (pool.querySelectorAll(".approved-team-chip").length === 0) {
    if (!pool.querySelector(".empty-pool-msg")) {
      pool.innerHTML =
        '<div class="empty-pool-msg">All teams assigned to matches!</div>';
    }
  } else {
    const emptyMsg = pool.querySelector(".empty-pool-msg");
    if (emptyMsg) emptyMsg.remove();
  }
}

function removeMatchRow(deleteBtn) {
  const row = deleteBtn.closest(".match-slot-row");
  row.querySelectorAll(".drop-zone.filled").forEach((dropZone) => {
    if (dropZone.dataset.teamId) {
      returnTeamToPool(
        dropZone.dataset.teamId,
        dropZone.dataset.teamName,
        dropZone.dataset.teamLogo
      );
    }
  });
  row.remove();
  checkEmptyPool();
}

function addNewMatchRow() {
  const wrapper = document.getElementById("matchRowsWrapper");
  const rowCount = wrapper.getElementsByClassName("match-slot-row").length + 1;

  const newRow = document.createElement("div");
  newRow.className = "match-slot-row";
  newRow.innerHTML = `
            <div class="match-number-badge">Match #${rowCount}</div>
            <select class="builder-select">
                <option value="ROUND_1">Round 1</option>
                <option value="ROUND_2">Round 2</option>
                <option value="ROUND_3">Round 3</option>
                <option value="QUARTER_FINALS">Quarter-Finals</option>
                <option value="SEMI_FINALS">Semi-Finals</option>
                <option value="FINAL">Final</option>
            </select>
            <div class="drop-zone" ondragover="allowDrop(event)" ondragleave="removeDropStyle(event)" ondrop="dropTeam(event, this)">
                <span>Drop Team A here</span>
            </div>
            <span class="vs-text">VS</span>
            <div class="drop-zone" ondragover="allowDrop(event)" ondragleave="removeDropStyle(event)" ondrop="dropTeam(event, this)">
                <span>Drop Team B here</span>
            </div>
            <input type="datetime-local" class="builder-input" value="2026-08-20T18:00">
            <input type="text" class="builder-input venue-input" placeholder="Venue (e.g. Arena A)">
            <button type="button" class="btn-icon-action" onclick="undoLastAction(this)" disabled title="Undo last placement">↩ Undo</button>
            <button class="btn-icon-delete" title="Remove Match" onclick="removeMatchRow(this)">&times;</button>
        `;
  wrapper.appendChild(newRow);
}
let currentDate = new Date(2026, 7, 15);

function updateDateDisplay() {
  const options = {
    weekday: "long",
    month: "long",
    day: "numeric",
  };

  document.getElementById("dateDay").textContent =
    currentDate.toLocaleDateString("en-US", options);
}

function changeDate(days) {
  currentDate.setDate(currentDate.getDate() + days);

  updateDateDisplay();

  showToast(
    "Showing schedule for " +
      currentDate.toLocaleDateString("en-US", {
        month: "short",
        day: "numeric",
      })
  );
}

/* =========================
       FILTER
    ========================= */

function setFilter(filter, button) {
  document
    .querySelectorAll(".filter")
    .forEach((btn) => btn.classList.remove("active"));

  button.classList.add("active");

  const rows = document.querySelectorAll(".schedule-row");

  rows.forEach((row) => {
    const status = row.dataset.status;

    if (filter === "all") {
      row.style.display = "grid";
    } else if (filter === "upcoming") {
      row.style.display = status === "upcoming" ? "grid" : "none";
    } else if (filter === "live") {
      row.style.display = status === "live" ? "grid" : "none";
    }
  });

  showToast(
    filter === "all" ? "Showing all matches" : "Showing " + filter + " matches"
  );
}

/* =========================
       VENUE FILTER
    ========================= */

function filterVenue(venue) {
  const cards = document.querySelectorAll(".match-card");

  cards.forEach((card) => {
    if (venue === "all") {
      card.parentElement.parentElement.style.display = "grid";
      return;
    }

    const info = card.querySelector(".match-info");

    const venueText = info ? info.textContent : "";

    card.parentElement.parentElement.style.display = venueText.includes(venue)
      ? "grid"
      : "none";
  });

  showToast(venue === "all" ? "Showing all venues" : "Filtered by " + venue);
}

/* =========================
       EDIT MODAL
    ========================= */

function openEditModal(match, teamA, teamB, time, venue) {
  document.getElementById("modalTeams").textContent = teamA + " vs " + teamB;

  document.getElementById("modalVenue").value = venue;

  document.getElementById("modal").classList.add("show");
}

// function closeModal() {
//   document.getElementById("modal").classList.remove("show");
// }

/* =========================
       SAVE
    ========================= */

function saveSchedule() {
  closeModal();

  showToast("Match schedule updated successfully");
}

/* =========================
       PUBLISH
    ========================= */

// Grab the Tournament ID dynamically from the URL or a hidden input attribute
function getTournamentId() {
  const segments = window.location.pathname.split("/");
  return segments[segments.length - 1]; // Assuming URL ends with /manage/{id}
}

// 1. Triggered when clicking the main Publish button
function publishSchedule() {
  const hasConflict = document.querySelector(".match-card.conflict");

  if (hasConflict) {
    showToast("Fix the venue conflict before publishing");
    return;
  }

  // Open Professional Confirmation Popup
  document.getElementById("confirmModal").style.display = "flex";
}

function closeConfirmModal() {
  document.getElementById("confirmModal").style.display = "none";
}

// 2. Triggered when organizer clicks "Yes, Publish" on the confirmation modal
function executePublishSchedule() {
  closeConfirmModal();

  const tournamentId = getTournamentId();

  // Hit your backend API endpoint that triggers the email service
  fetch(`/api/organizer/tournament/publish/${tournamentId}`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
  })
    .then((response) => {
      if (response.ok) {
        // Show Success Modal once backend finishes sending emails
        document.getElementById("successModal").style.display = "flex";
      } else {
        showToast("Failed to publish schedule. Please try again.");
      }
    })
    .catch((error) => {
      console.error("Error:", error);
      showToast("An error occurred while publishing.");
    });
}

/* =========================
       TOAST
    ========================= */

let toastTimer;

function showToast(message) {
  const toast = document.getElementById("toast");

  document.getElementById("toastMessage").textContent = message;

  toast.classList.add("show");

  clearTimeout(toastTimer);

  toastTimer = setTimeout(() => {
    toast.classList.remove("show");
  }, 2600);
}

/* =========================
       CLOSE MODAL ON OUTSIDE CLICK
    ========================= */

document.getElementById("modal").addEventListener("click", function (e) {
  if (e.target === this) {
    closeModal();
  }
});

/* =========================
       ESCAPE KEY
    ========================= */

document.addEventListener("keydown", function (e) {
  if (e.key === "Escape") {
    closeModal();
  }
});

updateDateDisplay();

// Edit pop up of match

// Open the Edit Modal and pre-fill values
function openEditModal(
  matchId,
  matchNumber,
  teamA,
  teamB,
  matchDate,
  matchTime,
  venueLocation
) {
  // 1. Set title header
  document.getElementById(
    "editMatchTitle"
  ).value = `Match #${matchNumber}: ${teamA} vs ${teamB}`;
  document.getElementById("editMatchId").value = matchId;

  // 2. Properly combine Match Date and Match Time for the datetime-local input (e.g., "2026-08-20T18:00")
  if (matchDate && matchTime) {
    // Format time to HH:mm if it includes seconds
    let formattedTime =
      matchTime.length > 5 ? matchTime.substring(0, 5) : matchTime;
    document.getElementById(
      "editMatchDatetime"
    ).value = `${matchDate}T${formattedTime}`;
  } else {
    document.getElementById("editMatchDatetime").value = "";
  }

  // 3. Set the current venue location correctly
  document.getElementById("editMatchVenue").value =
    venueLocation && venueLocation !== "null" ? venueLocation : "";

  // 4. Show modal
  document.getElementById("editMatchModal").style.display = "flex";
}

// Close the Edit Modal
function closeEditModal() {
  document.getElementById("editMatchModal").style.display = "none";
}

// Submit edited details via AJAX/Fetch to your backend
async function submitEditMatch(event) {
  event.preventDefault();
  const matchId = document.getElementById("editMatchId").value;
  const datetime = document.getElementById("editMatchDatetime").value;
  const venue = document.getElementById("editMatchVenue").value;

  try {
    // 1. Immediately close the edit modal popup the second they click save
    closeEditModal();

    // 2. Send data to your Spring Boot backend API in the background
    const response = await fetch("/api/match/update", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ matchId, datetime, venue }),
    });

    if (!response.ok) {
      throw new Error("Failed to update match schedule.");
    }

    // 3. Silently reload the page to show changes instantly without annoying popups
    location.reload();
  } catch (error) {
    // Only show an error alert if something actually fails on the server
    Swal.fire({
      title: "Error!",
      text: error.message || "Something went wrong.",
      icon: "error",
      background: "#141417",
      color: "#fff",
      confirmButtonColor: "#ff914d",
    });
  }
}
// Three-dot Options Menu handler
function toggleOptionsMenu(buttonElement) {
  // Logic to toggle a small contextual dropdown popup for quick actions (Reschedule, Delete, etc.)
  alert("Options menu clicked for this fixture slot.");
}

async function loadLiveScoring(event) {
  event.preventDefault();

  const clickedLink = event.currentTarget;

  const tournamentId = getTournamentId();

  // 1. Fetch the Live Scoring content from the server
  const response = await fetch(
    `/api/organizer/tournament/${tournamentId}/live-scoring`
  );
  const html = await response.text();

  // 2. Put that content inside the tab-live-scoring section
  const liveScoringSection = document.getElementById("tab-live-scoring");
  liveScoringSection.innerHTML = html;

  // Run Live Scoring's own setup logic now that its HTML exists
  initLiveScoring();

  // 3. Hide ALL tab-panels (Overview, Participants, Brackets, Schedule, Settings, etc.)
  document.querySelectorAll(".tab-panel").forEach((panel) => {
    panel.classList.remove("active");
  });

  // 4. Show ONLY the Live Scoring tab
  liveScoringSection.classList.add("active");

  // 5. Update sidebar highlighting
  document.querySelectorAll(".sidebar-link").forEach((el) => {
    el.classList.remove("active");
  });
  clickedLink.classList.add("active");
}
