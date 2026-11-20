console.log("Dashboard js working.....");



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

document.addEventListener("DOMContentLoaded", function () {
  const popCtx = document.getElementById("popularityChart").getContext("2d");
  new Chart(popCtx, {
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

  const compCtx = document.getElementById("completionChart").getContext("2d");
  new Chart(compCtx, {
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
});


// IF CLICKED CANCEL: Simply hide the popup modal. They stay exactly where they are on the dashboard.
function closeModal() {
  document.getElementById("onboardingModal").style.display = "none";
}

// IF CLICKED CONTINUE: Redirect them straight to the basic-info form page
function goToBasicInfo() {
  window.location.href = "/api/organizer/basic-info";
}
