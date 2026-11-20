let timeLeft = 45;
let intervalId = null;

const clock = document.getElementById("countdownClock");
const timerContainer = document.getElementById("timerContainer");
const submitBtn = document.getElementById("submitButton");
const otpInput = document.getElementById("otpCode");
const resendWrapper = document.getElementById("resendWrapper");
const toastNotification = document.getElementById("toastNotification");

function startTimer() {
  // Reset element states
  timeLeft = 45;
  otpInput.disabled = false;
  otpInput.placeholder = "••••••";
  submitBtn.disabled = false;
  submitBtn.style.backgroundColor = "var(--primary)";
  submitBtn.style.color = "#000000";
  submitBtn.style.cursor = "pointer";
  submitBtn.innerText = "Verify & Continue ➔";
  timerContainer.innerHTML = `Time remaining: <span id="countdownClock" style="color: var(--primary); font-weight: bold;">${timeLeft}</span> seconds`;
  resendWrapper.style.display = "none";

  // Grab handle of our fresh inner nested clock container ref
  const liveClock = document.getElementById("countdownClock");

  intervalId = setInterval(() => {
    timeLeft--;
    if (liveClock) liveClock.textContent = timeLeft;

    if (timeLeft <= 0) {
      clearInterval(intervalId);

      // Code expired UI adjustments
      timerContainer.innerHTML =
        "<span style='color: var(--error); font-weight: 600;'>OTP has expired!</span>";
      otpInput.disabled = true;
      otpInput.placeholder = "EXPIRED";

      submitBtn.disabled = true;
      submitBtn.style.backgroundColor = "#2e2e38";
      submitBtn.style.color = "#a0a0ab";
      submitBtn.style.cursor = "not-allowed";
      submitBtn.innerText = "Timeout Expired";

      // Reveal the Resend Link interface seamlessly
      resendWrapper.style.display = "block";
    }
  }, 1000);
}

// Trigger background AJAX/Fetch to controller mapping
function handleResendOTP() {
  toastNotification.style.color = "var(--text-muted)";
  toastNotification.innerText = "Sending fresh token...";

  fetch("/api/organizer/resend-otp", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
  })
    .then((response) => response.json())
    .then((data) => {
      if (data.success) {
        toastNotification.style.color = "#4ade80"; // Bright Green Success
        toastNotification.innerText = data.message;

        // Restart everything fresh
        setTimeout(() => {
          toastNotification.innerText = "";
          startTimer();
        }, 1200);
      } else {
        toastNotification.style.color = "var(--error)";
        toastNotification.innerText = data.message;
      }
    })
    .catch((error) => {
      console.error("Error:", error);
      toastNotification.style.color = "var(--error)";
      toastNotification.innerText = "Connection drop error. Try again.";
    });
}

// Self-start on initial landing execution
document.addEventListener("DOMContentLoaded", startTimer);
