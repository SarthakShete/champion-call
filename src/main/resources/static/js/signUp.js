function togglePasswordVisibility() {
  const passwordInput = document.getElementById("password");
  const eyeIcon = document.querySelector(".toggle-password");

  // If it's currently hidden, show it
  if (passwordInput.type === "password") {
    passwordInput.type = "text";
    // Swap the FontAwesome icon classes
    eyeIcon.classList.remove("fa-eye");
    eyeIcon.classList.add("fa-eye-slash");
  }
  // If it's currently visible, hide it
  else {
    passwordInput.type = "password";
    // Swap the FontAwesome icon classes back
    eyeIcon.classList.remove("fa-eye-slash");
    eyeIcon.classList.add("fa-eye");
  }
}
