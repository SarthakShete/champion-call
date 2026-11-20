const navLinks = document.querySelectorAll(".side-nav__link");
const panels = document.querySelectorAll("[data-panel]");
const tournamentHero = document.querySelector("#tournament-hero");
const tournamentStats = document.querySelector("#tournament-stats");

function showView(view) {
  navLinks.forEach((link) => {
    link.classList.toggle("is-active", link.dataset.view === view);
  });

  panels.forEach((panel) => {
    panel.classList.toggle("is-visible", panel.dataset.panel === view);
  });

  const isOverviewView = view === "overview";
  if (tournamentHero) {
    tournamentHero.style.display = isOverviewView ? "" : "none";
  }
  if (tournamentStats) {
    tournamentStats.style.display = isOverviewView ? "" : "none";
  }
}

navLinks.forEach((link) => {
  link.addEventListener("click", (event) => {
    event.preventDefault();
    const view = link.dataset.view;
    showView(view);
    history.replaceState(null, "", `#${view}`);
  });
});

const initialView = location.hash.slice(1);
if ([...navLinks].some((link) => link.dataset.view === initialView)) {
  showView(initialView);
} else {
  showView("overview");
}

// 1. Popup Handlers for Guests and Unprofiled Players
function handleRoleRestriction(type) {
  if (type === "guest") {
    Swal.fire({
      title: "Login Required",
      text: "You need to log in as a player to participate or join a team.",
      icon: "info",
      background: "#18181b",
      color: "#ffffff",
      iconColor: "#ff7700",
      confirmButtonColor: "#ff6b00",
      confirmButtonText: "Log In",
    }).then((result) => {
      if (result.isConfirmed) {
        window.location.href = "/login";
      }
    });
  } else if (type === "no-profile") {
    Swal.fire({
      title: "Complete Your Profile",
      text: "You must complete your player profile before you can register or join teams.",
      icon: "warning",
      background: "#18181b",
      color: "#ffffff",
      iconColor: "#ff7700",
      confirmButtonColor: "#ff6b00",
      confirmButtonText: "Complete Profile",
    }).then((result) => {
      if (result.isConfirmed) {
        window.location.href = "/api/player/setup";
      }
    });
  }
}

// 2. Click handler for Join Team buttons
function handleJoinTeam(teamId) {
  const isLoggedIn = window.serverAuth?.isLoggedIn ?? false;
  const hasProfile = window.serverAuth?.hasProfile ?? false;

  if (!isLoggedIn) {
    handleRoleRestriction("guest");
    return;
  }

  if (!hasProfile) {
    handleRoleRestriction("no-profile");
    return;
  }

  window.location.href = "/api/teams/join/" + teamId;
}

// 3. Click handler for Registration Form Submit Button
function handleRegistrationSubmit() {
  const isLoggedIn = window.serverAuth?.isLoggedIn ?? false;
  const hasProfile = window.serverAuth?.hasProfile ?? false;

  if (!isLoggedIn) {
    handleRoleRestriction("guest");
    return;
  }

  if (!hasProfile) {
    handleRoleRestriction("no-profile");
    return;
  }

  const checkboxes = document.querySelectorAll(".rule-checkbox");
  const allChecked = Array.from(checkboxes).every((cb) => cb.checked);

  if (!allChecked || checkboxes.length === 0) {
    Swal.fire({
      icon: "warning",
      title: "Rules Required",
      text: "You must read and check all rules in the Rules & Format tab before registering your team!",
      confirmButtonText: "Go to Rules",
      background: "#18181b",
      color: "#ffffff",
      iconColor: "#ff7700",
      confirmButtonColor: "#ff6b00",
    }).then((result) => {
      if (result.isConfirmed) {
        showView("rules");
        history.replaceState(null, "", "#rules");
      }
    });
    return;
  }

  const form = document.getElementById("registration-form");

  if (form) {
    form.submit();
  }
}

document.addEventListener("DOMContentLoaded", function () {
  const checkboxes = document.querySelectorAll(".rule-checkbox");
  const proceedBtn = document.getElementById("proceedRegistrationBtn");

  function validateCheckboxes() {
    if (!proceedBtn) return;

    const allChecked = Array.from(checkboxes).every((cb) => cb.checked);

    if (allChecked && checkboxes.length > 0) {
      proceedBtn.disabled = false;
      proceedBtn.style.opacity = "1";
      proceedBtn.style.cursor = "pointer";
    } else {
      proceedBtn.disabled = true;
      proceedBtn.style.opacity = "0.5";
      proceedBtn.style.cursor = "not-allowed";
    }
  }

  checkboxes.forEach((cb) => {
    cb.addEventListener("change", validateCheckboxes);
  });

  if (proceedBtn) {
    proceedBtn.addEventListener("click", function (event) {
      event.preventDefault();
      showView("registration");
      history.replaceState(null, "", "#registration");
    });
  }

  const successMessage = window.serverMessages?.success;
  const errorMessage = window.serverMessages?.error;
  const tournamentName = window.serverMessages?.tournament || "Tournament";

  if (successMessage) {
    Swal.fire({
      icon: "success",
      title: `Your team is Registered Successfully for ${tournamentName}!`,
      text: successMessage,
      background: "#18181b",
      color: "#ffffff",
      iconColor: "#22c55e",
      confirmButtonColor: "#ff7700",
      confirmButtonText: "Awesome!",
    }).then((result) => {
      if (result.isConfirmed) {
        window.location.href = "/api/player/profile";
      }
    });
  }

  if (errorMessage) {
    Swal.fire({
      icon: "warning",
      title: "Registration Blocked",
      text: errorMessage,
      background: "#18181b",
      color: "#ffffff",
      iconColor: "#ff7700",
      confirmButtonColor: "#ff7700",
      confirmButtonText: "OK",
    });
  }

  validateCheckboxes();

  /* =========================================================
     MATCH SCHEDULE FILTER
     Filters match cards by status (all / upcoming / live /
     completed) and hides any date heading that has no visible
     cards left under it.
     ========================================================= */
  const filterButtons = document.querySelectorAll("#scheduleFilters button");
  const timeline = document.getElementById("scheduleTimeline");

  let emptyStateDiv = document.querySelector(".dynamic-empty-state");
  if (!emptyStateDiv && timeline && timeline.parentNode) {
    emptyStateDiv = document.createElement("div");
    emptyStateDiv.className = "empty-state dynamic-empty-state";
    emptyStateDiv.style.display = "none";
    emptyStateDiv.innerHTML = `
        <div class="empty-state-icon">🔍</div>
        <p>No matches found for this filter category.</p>
    `;
    timeline.parentNode.appendChild(emptyStateDiv);
  }

  function applyFilter(filter) {
    let visibleCount = 0;

    document
      .querySelectorAll("#scheduleTimeline .day-group")
      .forEach((group) => {
        let groupVisible = 0;

        group.querySelectorAll(".match-card").forEach((card) => {
          const show = filter === "all" || card.dataset.status === filter;
          card.style.display = show ? "flex" : "none";
          if (show) groupVisible++;
        });

        // Hide the whole date block if none of its matches are visible
        group.style.display = groupVisible ? "" : "none";
        visibleCount += groupVisible;
      });

    if (emptyStateDiv) {
      emptyStateDiv.style.display = visibleCount ? "none" : "block";
    }
    if (timeline) {
      timeline.style.display = visibleCount ? "block" : "none";
    }
  }

  filterButtons.forEach((button) => {
    button.addEventListener("click", () => {
      filterButtons.forEach((b) => b.classList.remove("active"));
      button.classList.add("active");
      applyFilter(button.dataset.filter);
    });
  });
});

function previewTeamLogo(event) {
  const file = event.target.files[0];
  const preview = document.getElementById("logoPreview");
  const placeholder = document.getElementById("uploadPlaceholderText");

  if (file) {
    const reader = new FileReader();

    reader.onload = function (e) {
      preview.src = e.target.result;
      preview.style.display = "block";
      placeholder.style.display = "none";
    };

    reader.readAsDataURL(file);
  } else {
    preview.src = "";
    preview.style.display = "none";
    placeholder.style.display = "block";
  }
}

let pendingRedirectUrl = "";

function openJoinTeamModal(teamId, teamName) {
  fetch(`/api/player/profile/status-check`)
    .then((response) => response.json())
    .then((data) => {
      if (!data.hasIdProof) {
        Swal.fire({
          title: "ID Proof Required",
          text: "You must submit and upload your ID proof before joining a team!",
          icon: "warning",
          background: "#18110e",
          color: "#ffffff",
          iconColor: "#ff7700",
          showCancelButton: true,
          confirmButtonColor: "#ff6b00",
          cancelButtonColor: "#374151",
          confirmButtonText: "Complete Verification",
          cancelButtonText: "Cancel",
        }).then((result) => {
          if (result.isConfirmed) {
            window.location.href = "/api/player/profile#identity-verification";
          }
        });
        return;
      }

      document.getElementById("modalTeamRegistrationId").value = teamId;
      const teamNameSpan = document.getElementById("modalTeamName");

      if (teamNameSpan) {
        teamNameSpan.innerText = teamName;
      }

      document.getElementById("inviteCodeInput").value = "";
      document.getElementById("joinTeamModal").style.display = "flex";
    })
    .catch((error) => {
      console.error("Error checking profile status:", error);
    });
}

function closeJoinTeamModal() {
  document.getElementById("joinTeamModal").style.display = "none";
}

function submitJoinTeam(event) {
  event.preventDefault();

  const teamId = document.getElementById("modalTeamRegistrationId").value;
  const inviteCode = document.getElementById("inviteCodeInput").value.trim();
  const teamName = document.getElementById("modalTeamName")
    ? document.getElementById("modalTeamName").innerText
    : "the team";

  fetch(`/api/teams/join`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      teamRegistrationId: teamId,
      inviteCode: inviteCode,
    }),
  })
    .then((response) => {
      if (response.ok) {
        return response.json();
      } else {
        return response.text().then((text) => {
          throw new Error(text);
        });
      }
    })
    .then((data) => {
      closeJoinTeamModal();
      pendingRedirectUrl = data.redirectUrl || "/api/player/profile";

      Swal.fire({
        title: "Successfully Joined!",
        text: `You have successfully joined ${teamName}!`,
        icon: "success",
        background: "#18110e",
        color: "#ffffff",
        confirmButtonColor: "#ff6b00",
        confirmButtonText: "OK",
        showCancelButton: true,
        cancelButtonText: "Cancel",
        cancelButtonColor: "#374151",
        customClass: {
          popup: "custom-swal-popup",
        },
      }).then((result) => {
        if (result.isConfirmed) {
          window.location.href = pendingRedirectUrl;
        } else {
          location.reload();
        }
      });
    })
    .catch((error) => {
      console.error("Error:", error);

      let errorMessage =
        error.message || "Invalid invite code or an error occurred.";
      let redirectUrl = null;

      try {
        const errJson = JSON.parse(error.message);
        if (errJson.message) {
          errorMessage = errJson.message;
        }
        if (errJson.redirectUrl) {
          redirectUrl = errJson.redirectUrl;
        }
      } catch (e) {
        // Fallback if error is plain text
      }

      Swal.fire({
        title: "Failed to Join",
        text: errorMessage,
        icon: "error",
        background: "#18181b",
        color: "#ffffff",
        iconColor: "#ff7700",
        showCancelButton: true,
        confirmButtonColor: "#ff6b00",
        cancelButtonColor: "#374151",
        confirmButtonText: redirectUrl ? "Upload ID Proof" : "OK",
        cancelButtonText: "Cancel",
      }).then((result) => {
        if (result.isConfirmed && redirectUrl) {
          window.location.href = redirectUrl;
        }
      });
    });
}

/* =========================================================
   MATCH DETAIL PANEL
   Now behaves like any other content-view panel (Overview /
   Schedule / Teams / etc.) driven through showView(), instead
   of being a separate fixed full-screen overlay.
   ========================================================= */
// Helper function to format round names properly
function formatRoundName(roundNumber) {
  if (!roundNumber) return "Group Stage";
  var upperRound = roundNumber.toUpperCase();

  // Convert ROUND_1, ROUND_2, and ROUND_3 all to Group Stage
  if (
    upperRound === "ROUND_1" ||
    upperRound === "ROUND_2" ||
    upperRound === "ROUND_3"
  ) {
    return "Group Stage";
  }

  // Format other rounds nicely (e.g., QUARTER_FINALS -> Quarter-Finals)
  return roundNumber
    .toLowerCase()
    .replace(/_/g, " ")
    .replace(/\b\w/g, function (l) {
      return l.toUpperCase();
    });
}

function openMatchDetail(cardEl) {
  var matchId = cardEl.getAttribute("data-match-id");

  if (!matchId) {
    console.error("Match ID is missing!");
    return;
  }

  // 🔄 Fetch dynamic match details from your backend API
  fetch(`/api/match/${matchId}/details`)
    .then(function (response) {
      if (!response.ok) {
        throw new Error("Failed to fetch match details");
      }
      return response.json();
    })
    .then(function (data) {
      // Extract data from backend response
      var teamA =
        data.teamAName || cardEl.getAttribute("data-team-a") || "Team A";
      var teamB =
        data.teamBName || cardEl.getAttribute("data-team-b") || "Team B";
      var time = data.matchTime || cardEl.getAttribute("data-time") || "";
      var venue =
        data.venueLocation || cardEl.getAttribute("data-venue") || "Venue TBD";

      // Format round using helper function
      var rawRound =
        data.roundNumber || cardEl.getAttribute("data-round") || "Group Stage";
      var round = formatRoundName(rawRound);

      var status = (
        data.status ||
        cardEl.getAttribute("data-match-status") ||
        "SCHEDULED"
      ).toUpperCase();
      var umpire = data.umpireName || "Not Assigned";

      var scoreA = data.teamAScore != null ? data.teamAScore : 0;
      var scoreB = data.teamBScore != null ? data.teamBScore : 0;

      // Populate text fields
      document.getElementById("mdTeamAName").textContent = teamA;
      document.getElementById("mdTeamBName").textContent = teamB;

      // Crests initials generator
      document.getElementById("mdCrestA").textContent = teamA
        .split(" ")
        .map(function (w) {
          return w[0];
        })
        .join("")
        .substring(0, 2)
        .toUpperCase();
      document.getElementById("mdCrestB").textContent = teamB
        .split(" ")
        .map(function (w) {
          return w[0];
        })
        .join("")
        .substring(0, 2)
        .toUpperCase();

      document.getElementById("mdSquadTeamAName").textContent = teamA;
      document.getElementById("mdSquadTeamBName").textContent = teamB;
      document.getElementById("mdSquadCrestA").textContent =
        document.getElementById("mdCrestA").textContent;
      document.getElementById("mdSquadCrestB").textContent =
        document.getElementById("mdCrestB").textContent;

      document.getElementById("mdTime").textContent = time || "--:--";
      document.getElementById("mdVenue").textContent = venue;
      document.getElementById("mdInfoVenue").textContent = venue;
      document.getElementById("mdRound").textContent = round;
      document.getElementById("mdInfoRound").textContent = round;

      // If you added an element for Umpire in your modal, update it too:
      var umpireEl = document.getElementById("mdUmpire");
      if (umpireEl) {
        umpireEl.textContent = umpire;
      }

      document.getElementById("mdTourneyTag").innerHTML =
        round +
        " &nbsp;·&nbsp; <strong>" +
        teamA +
        " vs " +
        teamB +
        "</strong>";

      // Status chip logic
      var chip = document.getElementById("mdStatusChip");
      var label = document.getElementById("mdStatusLabel");
      var pulse = document.getElementById("mdPulseDot");
      chip.classList.remove("md-live", "md-scheduled", "md-completed");

      if (status === "LIVE") {
        chip.classList.add("md-live");
        label.textContent = "Live";
        pulse.style.display = "inline-block";
        document.getElementById("mdScoreLine").textContent =
          scoreA + " - " + scoreB;
        document.getElementById("mdScoreSub").textContent = "Match in progress";
      } else if (status === "COMPLETED") {
        chip.classList.add("md-completed");
        label.textContent = "Full-time";
        pulse.style.display = "none";
        document.getElementById("mdScoreLine").textContent =
          scoreA + " - " + scoreB;
        document.getElementById("mdScoreSub").textContent = "Match completed";
      } else {
        chip.classList.add("md-scheduled");
        label.textContent = "Scheduled";
        pulse.style.display = "none";
        document.getElementById("mdScoreLine").textContent = "VS";
        document.getElementById("mdScoreSub").textContent =
          "Match yet to begin";
      }

      // Populate Team A and Team B Player Squad Lists dynamically if provided by backend
      populatePlayerList("mdSquadListA", data.teamAPlayers);
      populatePlayerList("mdSquadListB", data.teamBPlayers);

      // Reset to Overview tab each time it opens
      document
        .querySelectorAll("#matchDetailOverlay .md-tab-btn")
        .forEach(function (t) {
          t.classList.remove("md-active");
        });
      document
        .querySelectorAll("#matchDetailOverlay .md-panel")
        .forEach(function (p) {
          p.classList.remove("md-active");
        });
      document
        .querySelector(
          '#matchDetailOverlay .md-tab-btn[data-md-tab="mdOverview"]'
        )
        .classList.add("md-active");
      document.getElementById("mdOverview").classList.add("md-active");

      showView("matchDetail");
      history.replaceState(null, "", "#matchDetail");
      document.querySelectorAll(".side-nav__link").forEach(function (link) {
        link.classList.toggle("is-active", link.dataset.view === "schedule");
      });
    })
    .catch(function (error) {
      console.error("Error loading match details:", error);
    });
}

// Helper function to dynamically render player lists
function populatePlayerList(elementId, players) {
  var listContainer = document.getElementById(elementId);
  if (!listContainer) return;

  listContainer.innerHTML = "";
  if (!players || players.length === 0) {
    listContainer.innerHTML =
      "<li style='padding: 12px; color: #9ca3af; text-align: center;'>No players listed yet.</li>";
    return;
  }

  players.forEach(function (player) {
    var li = document.createElement("li");

    // Profile Avatar element
    var avatarImg =
      '<img src="' +
      (player.profilePic || "/images/default-avatar.png") +
      '" alt="Avatar" class="player-avatar-img" onerror="this.src=\'/images/default-avatar.png\'">';

    // Player Name element
    var nameSpan = '<span class="player-name-text">' + player.name + "</span>";

    var badgeHtml = "";
    if (player.isCaptain) {
      badgeHtml = '<span class="role-badge-c">Captain</span>';
    } else if (player.isViceCaptain) {
      badgeHtml = '<span class="role-badge-vc">Vice-Captain</span>';
    }

    li.innerHTML =
      '<div class="player-info-wrap">' +
      avatarImg +
      nameSpan +
      "</div>" +
      badgeHtml;
    listContainer.appendChild(li);
  });
}

function closeMatchDetail() {
  showView("schedule");
  history.replaceState(null, "", "#schedule");
}

document
  .querySelectorAll("#matchDetailOverlay .md-tab-btn")
  .forEach(function (tab) {
    tab.addEventListener("click", function () {
      document
        .querySelectorAll("#matchDetailOverlay .md-tab-btn")
        .forEach(function (t) {
          t.classList.remove("md-active");
        });
      document
        .querySelectorAll("#matchDetailOverlay .md-panel")
        .forEach(function (p) {
          p.classList.remove("md-active");
        });
      tab.classList.add("md-active");
      document
        .getElementById(tab.getAttribute("data-md-tab"))
        .classList.add("md-active");
    });
  });

document
  .querySelectorAll("#matchDetailOverlay .md-innings-pill")
  .forEach(function (p) {
    p.addEventListener("click", function () {
      document
        .querySelectorAll("#matchDetailOverlay .md-innings-pill")
        .forEach(function (x) {
          x.classList.remove("md-active");
        });
      p.classList.add("md-active");
    });
  });

// Escape key closes the match detail panel, but only while it is open
document.addEventListener("keydown", function (e) {
  const detail = document.getElementById("matchDetailOverlay");
  if (e.key === "Escape" && detail && detail.classList.contains("is-visible")) {
    closeMatchDetail();
  }
});
