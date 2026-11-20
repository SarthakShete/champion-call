console.log("My Tournament js is working.....");

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

document.addEventListener("DOMContentLoaded", () => {
  const filterForm = document.getElementById("filterForm");
  const searchInput = document.getElementById("tournamentSearch");
  const applyBtn = document.getElementById("applyFiltersBtn");
  const resultsContainer = document.getElementById(
    "tournament-results-container"
  );

  console.log("My Tournament page is called...");

  let currentPage = 0;

  // Global fetch function handling filters, search, and pagination
  window.fetchMyTournaments = function (page = 0) {
    currentPage = page;
    if (!filterForm || !resultsContainer) return;

    const formData = new FormData(filterForm);
    const params = new URLSearchParams(formData);

    params.set("page", currentPage);
    params.set("size", 9); // Matches your AppConstants.PAGE_SIZE

    resultsContainer.style.opacity = "0.5";

    fetch(`/api/organizer/tournament/myTournaments?${params.toString()}`, {
      headers: {
        "X-Requested-With": "XMLHttpRequest",
      },
    })
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

  // Live search with debounce (resets to page 0)
  if (searchInput) {
    let debounceTimer;
    searchInput.addEventListener("input", () => {
      clearTimeout(debounceTimer);
      debounceTimer = setTimeout(() => window.fetchMyTournaments(0), 300);
    });
  }

  // Button click (resets to page 0)
  if (applyBtn) {
    applyBtn.addEventListener("click", () => {
      window.fetchMyTournaments(0);
    });
  }
});
