console.log("Tournament List is working...");

/* chip filter active state */
document.querySelectorAll(".chip").forEach((chip) => {
  chip.addEventListener("click", () => {
    document
      .querySelectorAll(".chip")
      .forEach((c) => c.classList.remove("active"));
    chip.classList.add("active");
  });
});

// search

document.addEventListener("DOMContentLoaded", () => {
  const filterForm = document.getElementById("filterForm");
  const searchInput = document.getElementById("tournamentSearch");
  const statusSelect = document.getElementById("statusFilterSelect");
  const chipButtons = document.querySelectorAll("#sportChipGroup .chip");
  const selectedSportInput = document.getElementById("selectedSportInput");
  const resultsContainer = document.getElementById(
    "tournament-results-container"
  );

  let currentPage = 0;

  // Unified fetch function handling filters, search, and pagination parameters
  window.fetchTournaments = function (page = 0) {
    currentPage = page;
    console.log("Fetching tournaments for page: " + currentPage);
    if (!filterForm || !resultsContainer) return;

    const formData = new FormData(filterForm);
    const params = new URLSearchParams(formData);

    params.set("page", currentPage);
    params.set("size", 9); // Matches your AppConstants.PAGE_SIZE

    resultsContainer.style.opacity = "0.4";

    // Updated to hit the public search-tournament endpoint
    fetch(`/api/organizer/tournament/search-tournament?${params.toString()}`)
      .then((res) => {
        if (!res.ok) throw new Error("Network response was not ok");
        return res.text();
      })
      .then((html) => {
        resultsContainer.innerHTML = html;
        resultsContainer.style.opacity = "1";
        window.scrollTo({ top: 0, behavior: "smooth" });
      })
      .catch((err) => {
        console.error("Search Error:", err);
        resultsContainer.innerHTML =
          "<p style='text-align:center; color:#ef4444; grid-column: 1/-1; padding: 40px;'>Error loading tournaments. Please try again.</p>";
        resultsContainer.style.opacity = "1";
      });
  };

  // 1. Live text search with debounce (resets to page 0 on new search)
  if (searchInput) {
    let debounceTimer;
    searchInput.addEventListener("input", () => {
      clearTimeout(debounceTimer);
      debounceTimer = setTimeout(() => window.fetchTournaments(0), 300);
    });
  }

  // 2. Status Dropdown Change Handler (resets to page 0)
  if (statusSelect) {
    statusSelect.addEventListener("change", () => {
      window.fetchTournaments(0);
    });
  }

  // 3. Sport Chip Filter Click Handler (resets to page 0)
  chipButtons.forEach((btn) => {
    btn.addEventListener("click", () => {
      chipButtons.forEach((b) => b.classList.remove("active"));
      btn.classList.add("active");

      const sportValue = btn.getAttribute("data-sport");
      if (selectedSportInput) {
        selectedSportInput.value = sportValue === "all" ? "" : sportValue;
      }

      window.fetchTournaments(0);
    });
  });

  // 4. Tournament Details Page View Switcher (Sidebar Tabs)
  const navLinks = document.querySelectorAll(".side-nav__link");
  const panels = document.querySelectorAll(".content-view");

  if (navLinks.length > 0 && panels.length > 0) {
    navLinks.forEach((link) => {
      link.addEventListener("click", (e) => {
        const targetView = link.getAttribute("data-view");
        if (!targetView) return;

        // Remove active class from all links and panels
        navLinks.forEach((l) => l.classList.remove("is-active"));
        panels.forEach((p) => p.classList.remove("is-visible"));

        // Add active class to clicked link
        link.classList.add("is-active");

        // Show matching panel
        const activePanel = document.getElementById(targetView);
        if (activePanel) {
          activePanel.classList.add("is-visible");
        }
      });
    });
  }
});

function checkAllRulesVerified() {
  const checkboxes = document.querySelectorAll(".rule-checkbox");
  const proceedBtn = document.getElementById("proceedRegistrationBtn");

  if (!proceedBtn) return;

  // Check if every rule checkbox is checked
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
