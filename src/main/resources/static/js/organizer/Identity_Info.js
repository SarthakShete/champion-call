console.log("Identity js is workingg....");

document.getElementById("idDocument").addEventListener("change", function (e) {
  const file = e.target.files[0];
  const display = document.getElementById("fileNameDisplay");
  if (file) {
    display.textContent = file.name;
    display.style.display = "block";
  }
});

async function submitForm() {

  console.log("Submit form is called...");
  const formElement = document.getElementById("identityForm");
  const formData = new FormData(formElement);

  if (!formElement.checkValidity()) {
    formElement.reportValidity();
    return;
  }

  try {
    const response = await fetch("/api/organizer/do-Identity", {
      method: "POST",
      body: formData,
    });

    const result = await response.json();

    if (response.ok && result.success) {
      const swalResult = await showSweetAlert(
        "Identity Verified successfully!",
        "green"
      );

      // 2. Check if the user clicked OK
      if (swalResult.isConfirmed) {
        window.location.href = "/api/organizer/dashboard";
      }
    } else if (!result.success && result.requiresIdentity) {
      // Show the modal instead of a simple error alert
      showIdentityModal(result.message);
    } else {
      // Error Path (Includes Validation errors and Duplicate Name errors)
      const errorMessage =
        result.message || "Failed to save. Please check your inputs.";
      showSweetAlert(errorMessage, "red");
    }
  } catch (error) {
    console.error("Error submitting form:", error);
    alert("An error occurred while uploading.");
  }
}
function showSweetAlert(content, type) {
  let swalIcon = "info";
  let swalTitle = "Notice";

  if (type === "red") {
    swalIcon = "error";
    swalTitle = "Oops...";
  } else if (type === "green") {
    swalIcon = "success";
    swalTitle = "Success!";
  }

  return Swal.fire({
    icon: swalIcon,
    title: swalTitle,
    text: content,
    background: "#1f2937",
    color: "#ffffff",
    confirmButtonColor: "#ea580c",
    customClass: { popup: "rounded-xl" },
    // This ensures the popup waits for user interaction
    allowOutsideClick: false,
  });
}
