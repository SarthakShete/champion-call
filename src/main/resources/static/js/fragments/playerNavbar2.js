console.log("player navbar is called...");

// ==========================================
// PROFILE DROPDOWN MENU TOGGLE
// ==========================================
const profileToggle = document.getElementById("profileDropdownToggle");
const dropdownMenu = document.getElementById("profileDropdownMenu");

if (profileToggle && dropdownMenu) {
  profileToggle.addEventListener("click", (e) => {
    e.stopPropagation();
    const isVisible = dropdownMenu.style.display === "block";
    dropdownMenu.style.display = isVisible ? "none" : "block";
  });

  // Close dropdown when clicking outside
  document.addEventListener("click", () => {
    dropdownMenu.style.display = "none";
  });
}

// Handle Dashboard shortcut inside dropdown
// const menuDashboardBtn = document.getElementById("menuDashboardBtn");
// if (menuDashboardBtn) {
//   menuDashboardBtn.addEventListener("click", (e) => {
//     e.preventDefault();
//     showView("overview-view"); // Switches back to your overview/dashboard view
//   });
// }

// Handle Edit Profile shortcut inside dropdown
const menuEditProfileBtn = document.getElementById("menuEditProfileBtn");
if (menuEditProfileBtn) {
  menuEditProfileBtn.addEventListener("click", (e) => {
    e.preventDefault();
    showView("profile-view"); // Switches to profile view tab
  });
}
