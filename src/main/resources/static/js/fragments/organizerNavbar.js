// Global tracking variables to store active chart instances
let popularityChartInstance = null;
let completionChartInstance = null;

document.addEventListener("DOMContentLoaded", function () {
  const profileWidget = document.getElementById("userProfileDropdown");
  const dropdownMenu = document.getElementById("profileDropdownMenu");
  const chevronIcon = document.getElementById("chevronIcon");

  // Check if the profile exists and is complete right when the page loads
  if (
    window.organizerProfile &&
    window.organizerProfile.completeProfile === true
  ) {
    const modal = document.getElementById("onboardingModal");
    if (modal) {
      modal.style.display = "none";
      modal.classList.remove("show");
    }
  }

  if (profileWidget && dropdownMenu) {
    // Toggle dropdown state on click
    profileWidget.addEventListener("click", function (event) {
      // 1. If the user clicked inside the open menu links, let the browser process the redirect link
      if (
        dropdownMenu.classList.contains("show") &&
        dropdownMenu.contains(event.target)
      ) {
        return;
      }

      // 2. Toggle classes dynamically
      const isExpanded = dropdownMenu.classList.contains("show");

      if (!isExpanded) {
        dropdownMenu.classList.add("show");
        if (chevronIcon) chevronIcon.classList.add("rotate-chevron");
      } else {
        dropdownMenu.classList.remove("show");
        if (chevronIcon) chevronIcon.classList.remove("rotate-chevron");
      }
    });

    // Global Document Click: Auto-close dropdown if clicking completely outside the widget box
    document.addEventListener("click", function (event) {
      if (!profileWidget.contains(event.target)) {
        dropdownMenu.classList.remove("show");
        if (chevronIcon) chevronIcon.classList.remove("rotate-chevron");
      }
    });
  }

  // --- Notifications Dropdown Toggle Layer ---
  const notificationWrapper = document.querySelector(".notifications-dropdown");
  const bellBtn = document.querySelector(".bell-btn");
  const notificationList = document.querySelector(".notification-list");

  if (notificationWrapper && bellBtn && notificationList) {
    bellBtn.addEventListener("click", function (event) {
      event.stopPropagation();
      const isOpen = notificationList.style.display === "block";
      notificationList.style.display = isOpen ? "none" : "block";
    });

    // Prevent clicks inside the notification list from closing it immediately
    notificationList.addEventListener("click", function (event) {
      event.stopPropagation();
    });

    // Close notification dropdown when clicking anywhere else on the document
    document.addEventListener("click", function () {
      notificationList.style.display = "none";
    });
  }

  // --- Charts Initialization Layer ---
  const popularityElem = document.getElementById("popularityChart");
  if (popularityElem) {
    const popCtx = popularityElem.getContext("2d");

    if (popularityChartInstance) {
      popularityChartInstance.destroy();
    }

    popularityChartInstance = new Chart(popCtx, {
      type: "doughnut",
      data: {
        labels: ["Cricket", "Football", "Badminton", "Basketball"],
        datasets: [
          {
            data: [40, 25, 20, 15],
            backgroundColor: ["#ff914d", "#3b38ff", "#10b981", "#a855f7"],
            borderWidth: 0,
          },
        ],
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            position: "bottom",
            labels: { color: "#e0e0e0", font: { family: "Inter" } },
          },
        },
        cutout: "70%",
      },
    });
  }

  const completionElem = document.getElementById("completionChart");
  if (completionElem) {
    const compCtx = completionElem.getContext("2d");

    if (completionChartInstance) {
      completionChartInstance.destroy();
    }

    completionChartInstance = new Chart(compCtx, {
      type: "line",
      data: {
        labels: ["W1", "W2", "W3", "W4", "W5", "W6"],
        datasets: [
          {
            label: "Completion %",
            data: [62, 70, 68, 80, 89, 92],
            borderColor: "#10b981",
            backgroundColor: "rgba(16, 185, 129, 0.1)",
            fill: true,
            tension: 0.4,
            borderWidth: 2,
            pointRadius: 4,
            pointBackgroundColor: "#10b981",
          },
        ],
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: { legend: { display: false } },
        scales: {
          y: {
            min: 0,
            max: 100,
            ticks: { stepSize: 25, color: "#a0a0a0" },
            grid: { color: "rgba(255, 255, 255, 0.05)" },
          },
          x: {
            ticks: { color: "#a0a0a0" },
            grid: { display: false },
          },
        },
      },
    });
  }

  // --- Restore desktop collapsed sidebar state on load ---
  const sidebarEl = document.querySelector(".sidebar");
  if (sidebarEl && window.innerWidth > 900) {
    if (localStorage.getItem("cc_sidebar_collapsed") === "1") {
      sidebarEl.classList.add("collapsed");
    }
  }

  // If the window is resized back to desktop, make sure the mobile
  // open/overlay state is cleared so it doesn't stick around.
  window.addEventListener("resize", function () {
    if (window.innerWidth > 900) {
      const sb = document.querySelector(".sidebar");
      const overlay = document.getElementById("mobileOverlay");
      if (sb) sb.classList.remove("open");
      if (overlay) overlay.classList.remove("active");
    }
  });
});

// ===== Sidebar Toggle (called via inline onclick in the fragment) =====
// This MUST be a global function (not addEventListener) because the
// fragment's hamburger button and #mobileOverlay both use onclick="toggleSidebar()".
function toggleSidebar() {
  const sidebarEl = document.querySelector(".sidebar");
  const overlayEl = document.getElementById("mobileOverlay");
  if (!sidebarEl) return;

  const isMobile = window.innerWidth <= 900;

  if (isMobile) {
    // Mobile: slide sidebar in/out + toggle dark overlay behind it
    sidebarEl.classList.toggle("open");
    if (overlayEl) overlayEl.classList.toggle("active");
  } else {
    // Desktop: collapse to icon-only rail, remember the choice
    const collapsed = sidebarEl.classList.toggle("collapsed");
    localStorage.setItem("cc_sidebar_collapsed", collapsed ? "1" : "0");
  }
}

// This triggers when they click a locked "New Tournament" button
// (i.e. organizer hasn't completed their profile yet)
function completeProfile(event) {
  if (event) event.preventDefault();

  // If your backend profile data exists and is complete, don't show the modal!
  if (
    window.organizerProfile &&
    window.organizerProfile.completeProfile === true
  ) {
    return;
  }

  const modal =
    document.getElementById("onboardingModal") ||
    document.querySelector(".modal-overlay");

  if (modal) {
    if (modal.parentNode !== document.body) {
      document.body.appendChild(modal);
    }
    modal.classList.add("show");
  } else {
    window.location.href = "/api/organizer/basic-info";
  }
}
