/* ===== CONFIG ===== */
const DEMO = false; // CHANGED: backend is ready now
const API = {
  info: "/scorer/link/", // CHANGED: GET /scorer/link/{token}
  send: "/scorer/send-otp", // CHANGED: was /api/scorer/request-otp
  verify: "/scorer/verify-otp", // CHANGED: was /api/scorer/verify-otp
};
const SCORING_PAGE = (matchId) => `/scorer/match/${matchId}`;
const OTP_SECONDS = 300,
  RESEND_SECONDS = 30;
  /* ===== HELPERS ===== */

const $ = (id) => document.getElementById(id);
const token = new URLSearchParams(location.search).get("token") || "";
let mobile = "",
  tick = null,
  resendTick = null,
  matchId = null;

function show(view) {
  ["v1", "v2", "v3"].forEach((v) =>
    $(v).classList.toggle("active", v === view)
  );
  $("s1").className = "step " + (view === "v1" ? "active" : "done");
  $("s2").className =
    "step " + (view === "v2" ? "active" : view === "v3" ? "done" : "");
}
function showErr(id, text) {
  const e = $(id);
  e.textContent = text;
  e.classList.add("show");
}
function clearErr() {
  document.querySelectorAll(".msg").forEach((m) => m.classList.remove("show"));
  $("otp").classList.remove("error");
}
const initials = (n) =>
  n
    .split(/\s+/)
    .map((w) => w[0])
    .join("")
    .slice(0, 2)
    .toUpperCase();

async function api(url, body) {
  if (DEMO) {
    await new Promise((r) => setTimeout(r, 600));
    return { ok: true, data: { sessionToken: "demo-token", matchId: 1 } }; // CHANGED: sessionToken
  }
  const res = await fetch(url, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  });
  let data = null;
  try {
    data = await res.json();
  } catch (e) {}
  return { ok: res.ok, data };
}
function busy(btn, on, label) {
  btn.disabled = on;
  if (label) btn.textContent = on ? label : btn.dataset.label;
}

/* ===== MATCH CARD ===== */
// CHANGED: backend sends one text, "Team A vs Team B", so we split it
function fillMatch(m) {
  const parts = (m.matchTitle || "").split(" vs ");
  const teamA = parts[0] || "Team A";
  const teamB = parts[1] || "Team B";

  $("teamA").textContent = teamA;
  $("teamB").textContent = teamB;
  $("avA").textContent = initials(teamA);
  $("avB").textContent = initials(teamB);
  $("matchMeta").textContent = ""; // date and venue are not sent by the backend yet
  $("doneMatch").textContent = m.matchTitle || teamA + " vs " + teamB;
}

(async function loadInfo() {
  if (DEMO) {
    fillMatch({ matchTitle: "Malvani Strikers vs Kalyan Blasters" });
    return;
  }
  if (!token) return;
  try {
    // CHANGED: token goes in the URL path, not in ?token=
    const r = await fetch(API.info + encodeURIComponent(token));
    if (r.ok) {
      fillMatch(await r.json());
    } else {
      // CHANGED: bad, expired or revoked link, so block the form
      $("sendBtn").disabled = true;
      showErr(
        "err1",
        "This link is invalid or has expired. Ask your organizer for a new link."
      );
    }
  } catch (e) {}
})();

/* ===== TIMERS ===== */
function startTimers() {
  clearInterval(tick);
  clearInterval(resendTick);
  let left = OTP_SECONDS,
    wait = RESEND_SECONDS;
  const fmt = (s) =>
    String(Math.floor(s / 60)).padStart(2, "0") +
    ":" +
    String(s % 60).padStart(2, "0");
  $("timer").textContent = "Code expires in " + fmt(left);
  $("resendBtn").disabled = true;
  $("resendBtn").textContent = "Resend in " + wait + "s";
  tick = setInterval(() => {
    left--;
    $("timer").textContent =
      left > 0
        ? "Code expires in " + fmt(left)
        : "Code expired. Request a new one.";
    if (left <= 0) clearInterval(tick);
  }, 1000);
  resendTick = setInterval(() => {
    wait--;
    if (wait > 0) $("resendBtn").textContent = "Resend in " + wait + "s";
    else {
      clearInterval(resendTick);
      $("resendBtn").disabled = false;
      $("resendBtn").textContent = "Resend OTP";
    }
  }, 1000);
}

/* ===== STEP 1: SEND OTP ===== */
$("mobile").addEventListener(
  "input",
  (e) => (e.target.value = e.target.value.replace(/\D/g, ""))
);
async function sendOtp(fromResend) {
  clearErr();
  const val = fromResend ? mobile : $("mobile").value.trim();
  if (!/^[6-9]\d{9}$/.test(val))
    return showErr("err1", "Enter a valid 10-digit mobile number.");
  const btn = $("sendBtn");
  btn.dataset.label = "Send OTP";
  busy(btn, true, "Sending...");
  const r = await api(API.send, { token, mobileNumber: val });
  busy(btn, false, true);
  if (!r.ok)
    return showErr(
      fromResend ? "err2" : "err1",
      "We could not verify these details. Check the number or ask your organizer for a new link."
    );
  mobile = val;
  $("maskedNo").textContent =
    "+91 " + val.slice(0, 2) + "******" + val.slice(-2);
  show("v2");
  startTimers();
  document.querySelectorAll("#otp input").forEach((i) => (i.value = ""));
  $("verifyBtn").disabled = true;
  $("otp").children[0].focus();
}
$("sendBtn").onclick = () => sendOtp(false);
$("resendBtn").onclick = () => sendOtp(true);
$("mobile").addEventListener("keydown", (e) => {
  if (e.key === "Enter") sendOtp(false);
});
$("changeNo").onclick = () => {
  clearErr();
  clearInterval(tick);
  clearInterval(resendTick);
  show("v1");
  $("mobile").focus();
};

/* ===== STEP 2: OTP BOXES ===== */
const boxes = [...document.querySelectorAll("#otp input")];
const code = () => boxes.map((b) => b.value).join("");
boxes.forEach((b, i) => {
  b.addEventListener("input", () => {
    b.value = b.value.replace(/\D/g, "").slice(-1);
    if (b.value && i < 5) boxes[i + 1].focus();
    $("verifyBtn").disabled = code().length !== 6;
    clearErr();
  });
  b.addEventListener("keydown", (e) => {
    if (e.key === "Backspace" && !b.value && i > 0) boxes[i - 1].focus();
    if (e.key === "Enter" && code().length === 6) verify();
  });
  b.addEventListener("paste", (e) => {
    const t = (e.clipboardData.getData("text") || "")
      .replace(/\D/g, "")
      .slice(0, 6);
    if (!t) return;
    e.preventDefault();
    [...t].forEach((c, k) => (boxes[k].value = c));
    boxes[Math.min(t.length, 5)].focus();
    $("verifyBtn").disabled = t.length !== 6;
  });
});

async function verify() {
  clearErr();
  const btn = $("verifyBtn");
  btn.dataset.label = "Verify and continue";
  busy(btn, true, "Verifying...");
  const r = await api(API.verify, { token, otp: code() });
  if (!r.ok) {
    busy(btn, false, true);
    $("otp").classList.add("error");
    return showErr(
      "err2",
      "That code is not correct or has expired. Try again or request a new OTP."
    );
  }
  clearInterval(tick);
  clearInterval(resendTick);
  try {
    // CHANGED: backend sends "sessionToken", not "token"
    sessionStorage.setItem("scorerToken", r.data.sessionToken);
    sessionStorage.setItem("scorerMatchId", r.data.matchId);
  } catch (e) {}
  matchId = r.data.matchId;
  show("v3");
  setTimeout(go, 2500);
}
function go() {
  if (!DEMO) location.href = SCORING_PAGE(matchId);
}
$("verifyBtn").onclick = verify;
$("goBtn").onclick = go;

/* Missing token guard */
if (!token && !DEMO) {
  $("sendBtn").disabled = true;
  showErr(
    "err1",
    "This link is incomplete. Open the link from your assignment email again."
  );
}
