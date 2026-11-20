document.addEventListener("DOMContentLoaded", function () {
  // 1. Grab Avatar Elements Safely (Updated to match id="orgName" from HTML)
  const orgNameInput = document.getElementById("orgName");
  const avatarInitials = document.getElementById("avatarInitials");
  const avatarPreview = document.getElementById("avatarPreview");
  const photoUpload = document.getElementById("photoUpload");

  // Helper function to generate initials
  function getInitials(name) {
    // If the user hasn't typed anything yet, return your app logo fallback initials "CC"
    if (!name.trim()) return "CC";

    const parts = name.trim().split(" ");
    const first = parts[0] ? parts[0][0] : "";
    const last = parts.length > 1 ? parts[parts.length - 1][0] : "";
    return (first + last).toUpperCase();
  }

  // Initials Event Listener
  if (orgNameInput && avatarInitials && avatarPreview) {
    orgNameInput.addEventListener("input", function () {
      // Updates initials dynamically as long as an image isn't explicitly shown
      if (avatarPreview.style.display !== "block") {
        avatarInitials.textContent = getInitials(this.value);
      }
    });
  }

  // Photo Upload Preview Event Listener
  if (photoUpload && avatarPreview && avatarInitials) {
    photoUpload.addEventListener("change", function (e) {
      const file = e.target.files[0];
      if (file) {
        const reader = new FileReader();
        reader.onload = function (evt) {
          avatarPreview.src = evt.target.result;
          avatarPreview.style.display = "block";
          avatarInitials.style.display = "none";
        };
        reader.readAsDataURL(file);
      }
    });
  }

  // 2. SweetAlert Popups Logic
  const messageEl = document.getElementById("message-provider");

  if (messageEl) {
    // ⚠️ FIXED: Added these two lines back to read data attributes!
    const successMsg = messageEl.getAttribute("data-success");
    const errorMsg = messageEl.getAttribute("data-error");

    // Handle Success Popup
    if (successMsg && successMsg !== "null" && successMsg.trim() !== "") {
      Swal.fire({
        title: "Success!",
        text: successMsg,
        icon: "success",
        iconColor: "var(--success-green)",
        background: "var(--bg-card)",
        color: "var(--text-light)",
        confirmButtonColor: "var(--primary-orange)",
        confirmButtonText: "OK",
        allowOutsideClick: false,
        customClass: {
          popup: "custom-swal-border",
        },
      }).then((result) => {
        if (result.isConfirmed) {
          window.location.href = "/api/organizer/dashboard";
        }
      });
    }

    // Handle Error Popup
    if (errorMsg && errorMsg !== "null" && errorMsg.trim() !== "") {
      Swal.fire({
        title: "Error!",
        text: errorMsg,
        icon: "error",
        iconColor: "var(--danger-red)",
        background: "var(--bg-card)",
        color: "var(--text-light)",
        confirmButtonColor: "var(--danger-red)",
        confirmButtonText: "Try Again",
        customClass: {
          popup: "custom-swal-border",
        },
      });
    }
  }
});
