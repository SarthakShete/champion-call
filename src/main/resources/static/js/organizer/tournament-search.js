console.log("The Tournament Search js is working.....");

document.addEventListener("DOMContentLoaded", () => {
  const filterForm = document.getElementById("filterForm");
  const resultsContainer = document.getElementById(
    "tournament-results-container"
  );

  // Function to fetch filtered results
  function fetchResults() {
    const formData = new FormData(filterForm);
    const params = new URLSearchParams(formData);

    fetch(`/api/organizer/tournament/search?${params.toString()}`)
      .then((res) => res.text())
      .then((html) => {
        // --- ADDED THIS SECTION ---
        console.log("Fragment received:", html); // Debug: Check console to see what the server sent

        if (html.trim().length === 0) {
          resultsContainer.innerHTML = "<p>No tournaments found.</p>";
        } else {
          resultsContainer.innerHTML = html;
        }
        // --------------------------
      })
      .catch((err) => console.error("Error:", err));
  }

  // 1. Live Search (Keyword)
  const searchInput = document.getElementById("tournamentSearch");
  let debounceTimer;
  searchInput.addEventListener("input", () => {
    clearTimeout(debounceTimer);
    debounceTimer = setTimeout(fetchResults, 300);
  });

  // 2. Prevent form reload on "Apply" click
  filterForm.addEventListener("submit", (e) => {
    e.preventDefault();
    fetchResults();
  });
});
