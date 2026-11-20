/* =========================================================
   LIVE SCORING PAGE — full script
   Cards are server-rendered by Thymeleaf. Match info is read from
   the DOM (getMatch); scorer info lives in scorerByMatch.

   Button rules per match card:
   - No scorer     : [Manage Scoring] [Assign Scorer]
   - Scorer active : [Manage Scoring] [Manage Scorer]

   Manage Scoring  (ALWAYS asks for confirmation first)
     * no scorer     -> Confirmation popup -> OK
                        -> call GET /scorer/managing-score and show the
                        returned fragment (organizer scoring)
     * active scorer -> Confirmation popup -> OK
                        -> revoke scorer -> call managing-score

   Manage Scorer (modal)
     * Resend Access
     * Replace Scorer  -> assign form in "replace" mode
     * Revoke Access   -> Revoke Confirmation

   After revoke / takeover: [Assign Scorer] comes back
   ========================================================= */

/* ---------------------------------------------------------
   0. CONFIG  (change these if your paths differ)
--------------------------------------------------------- */
const scriptPromises = {};
const MANAGING_SCORE_URL = "/scorer/managing-score"; // returns the fragment HTML
const CRICKET_WS_JS = "/js/fragments/Managing_Scoring/cricket-websocket.js";
const SCOREKEEPER_JS = "/js/fragments/Managing_Scoring/Managing_Scoring.js"; // script that defines initScorekeeper()

/* ---------------------------------------------------------
   1. STATE
--------------------------------------------------------- */
let activity = [
  { text: "Rahul Patil verified scoring access", time: "2 minutes ago" },
  { text: "Match #5 went LIVE", time: "14 minutes ago" },
  { text: "Scorer assigned to Match #6", time: "35 minutes ago" },
  { text: "Match #4 completed", time: "1 hour ago" },
  { text: "Match #3 completed", time: "2 hours ago" },
];

let activeMatchId = null; // match used by the open modal / scoring page
let replaceMode = false; // true while the assign form is used to replace a scorer
let scoringBusy = false; // blocks double clicks while managing-score loads
let searchQuery = "";
let activeFilters = { status: "", date: "", scorer: "" };

/* matchId -> scorer record */
const scorerByMatch = {};

const SA_STATUS_LABELS = {
  awaiting: "Awaiting Verification",
  active: "Access Active",
  expired: "Access Expired",
  revoked: "Access Revoked",
};

const SA_DURATION_LABELS = {
  UNTIL_MATCH_ENDS: "Until Match Ends",
  TWO_HOURS: "2 Hours",
  SIX_HOURS: "6 Hours",
  TWENTY_FOUR_HOURS: "24 Hours",
};

/* ---------------------------------------------------------
   2. HELPERS
--------------------------------------------------------- */
function getMatch(id) {
  const card = document.querySelector(`[data-match-id="${id}"]`);
  if (!card) return null;

  const metaSpans = card.querySelectorAll(".match-meta span");
  const teamNames = card.querySelectorAll(".team-block .team-name");

  return {
    id: id,
    number: id,
    team1: teamNames[0]?.textContent.trim(),
    team2: teamNames[1]?.textContent.trim(),
    dateTime: metaSpans[0]?.textContent.trim() || "", // "22 Aug 2026 • 06:00 AM"
    venue: metaSpans[1]?.textContent.trim() || "",
    status: getCardStatusKey(card),
  };
}

/* Hide / show the layout's "Back to Tournaments" link while scoring is open */
function toggleTournamentBackLink(hide) {
  document.querySelectorAll("a, button").forEach((node) => {
    if (node.closest("#managingScoringHost")) return; // never touch our own back button
    if (node.textContent.trim().toLowerCase() === "back to tournaments") {
      node.style.display = hide ? "none" : "";
    }
  });
}

function getCardStatusKey(card) {
  const t =
    card.querySelector(".badge")?.textContent.trim().toLowerCase() || "";
  if (t.includes("live")) return "live";
  if (t.includes("complete")) return "completed";
  return "upcoming"; // "scheduled"
}

function initials(name) {
  return String(name || "")
    .split(" ")
    .filter(Boolean)
    .slice(0, 2)
    .map((w) => w[0].toUpperCase())
    .join("");
}

function maskMobile(mobile) {
  return "******" + String(mobile).slice(-4);
}

function el(tag, className, html) {
  const node = document.createElement(tag);
  if (className) node.className = className;
  if (html !== undefined) node.innerHTML = html;
  return node;
}

function escapeHtml(value) {
  return String(value ?? "").replace(
    /[&<>"']/g,
    (c) =>
      ({
        "&": "&amp;",
        "<": "&lt;",
        ">": "&gt;",
        '"': "&quot;",
        "'": "&#39;",
      }[c])
  );
}

function formatWhen(value) {
  if (!value) return "—";
  const d = new Date(value);
  if (isNaN(d.getTime())) return String(value);
  return d.toLocaleString("en-IN", {
    day: "2-digit",
    month: "short",
    year: "numeric",
    hour: "numeric",
    minute: "2-digit",
  });
}

/* Bind a listener only once per element (safe if init runs twice) */
function bindOnce(id, eventName, handler) {
  const node = document.getElementById(id);
  if (!node) return;
  const key = "bound" + eventName;
  if (node.dataset[key]) return;
  node.dataset[key] = "1";
  node.addEventListener(eventName, handler);
}

/* ---------------------------------------------------------
   3. STATUS LINE MARKUP
--------------------------------------------------------- */
function scoringStatusMarkup(scorer) {
  const map = {
    awaiting: { cls: "status-awaiting", label: "Awaiting Verification" },
    active: { cls: "status-active", label: "Access Active" },
    expired: { cls: "status-expired", label: "Access Expired" },
    revoked: { cls: "status-revoked", label: "Access Revoked" },
  };
  const s = map[scorer.status] || map.awaiting;
  return `<span class="status-line ${s.cls}"><span class="status-dot"></span>${s.label}</span>`;
}

/* ---------------------------------------------------------
   4. ACTIVITY PANEL
--------------------------------------------------------- */
function renderActivity() {
  const list = document.getElementById("activityTimeline");
  if (!list) return;
  list.innerHTML = "";
  activity.forEach((item) => {
    const li = el("li");
    li.innerHTML = `<span class="activity-text">${escapeHtml(
      item.text
    )}</span><span class="activity-time">${escapeHtml(item.time)}</span>`;
    list.appendChild(li);
  });
}

function logActivity(text) {
  activity.unshift({ text, time: "Just now" });
  activity = activity.slice(0, 8);
  renderActivity();
}

/* ---------------------------------------------------------
   5. MODAL HELPERS
--------------------------------------------------------- */
function openModal(overlayId) {
  const overlay = document.getElementById(overlayId);
  if (!overlay) return;

  overlay.hidden = false;
  document.body.style.overflow = "hidden";

  const focusable = overlay.querySelector("input, select, button");
  if (focusable) focusable.focus();
}

function closeModal(overlay) {
  if (!overlay) return;
  overlay.hidden = true;

  const anyOpen = Array.from(document.querySelectorAll(".modal-overlay")).some(
    (o) => !o.hidden
  );
  if (!anyOpen) document.body.style.overflow = "";
}

function closeAllModals() {
  document.querySelectorAll(".modal-overlay").forEach((o) => (o.hidden = true));
  document.body.style.overflow = "";
}

/* X button, Cancel/Close buttons and backdrop click */
document.addEventListener("click", (e) => {
  const overlay = e.target.closest(".modal-overlay");
  if (!overlay) return;

  const isBackdrop = e.target === overlay;
  const isCloseBtn = e.target.closest("[data-close-modal]");

  if (isBackdrop || isCloseBtn) {
    e.preventDefault();
    closeModal(overlay);
  }
});

document.addEventListener("keydown", (e) => {
  if (e.key === "Escape") closeAllModals();
});

/* ---------------------------------------------------------
   6. TOAST + SUCCESS POPUP
--------------------------------------------------------- */
function showToast(type, title, body) {
  const container = document.getElementById("toastContainer");
  if (!container) return;
  const toast = el("div", `toast${type === "danger" ? " toast-danger" : ""}`);

  const icon =
    type === "danger"
      ? '<svg width="14" height="14" viewBox="0 0 24 24" fill="none"><path d="M12 9v4m0 4h.01M10.3 3.9 2.5 17a2 2 0 0 0 1.7 3h15.6a2 2 0 0 0 1.7-3L13.7 3.9a2 2 0 0 0-3.4 0Z" stroke="#f87171" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"/></svg>'
      : '<svg width="14" height="14" viewBox="0 0 24 24" fill="none"><path d="m5 13 4 4L19 7" stroke="#22c55e" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"/></svg>';

  toast.innerHTML = `
    <div class="toast-title">${icon} ${title}</div>
    ${body ? `<div class="toast-body">${body}</div>` : ""}
  `;

  container.appendChild(toast);

  setTimeout(() => {
    toast.classList.add("toast-out");
    setTimeout(() => toast.remove(), 220);
  }, 4200);
}

function showSuccessPopup(title, message) {
  const popup = document.createElement("div");

  popup.innerHTML = `
    <div class="success-popup-overlay">
      <div class="success-popup">
        <div class="success-icon">✓</div>
        <h2>${escapeHtml(title)}</h2>
        <p>${escapeHtml(message)}</p>
        <button id="successPopupOk">OK</button>
      </div>
    </div>
  `;

  document.body.appendChild(popup);

  document.getElementById("successPopupOk").addEventListener("click", () => {
    popup.remove();
  });
}

/* ---------------------------------------------------------
   7. SEARCH + FILTER (works on the server-rendered cards)
--------------------------------------------------------- */
function parseCardDate(card) {
  const txt = card.querySelector(".match-meta span")?.textContent || "";
  const d = new Date(txt.split("•")[0].trim());
  return isNaN(d.getTime()) ? null : d;
}

function applyCardFilters() {
  const q = searchQuery.trim().toLowerCase();
  const { status, date, scorer } = activeFilters;

  const now = new Date();
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());
  const weekStart = new Date(today);
  weekStart.setDate(today.getDate() - ((today.getDay() + 6) % 7)); // Monday
  const weekEnd = new Date(weekStart);
  weekEnd.setDate(weekStart.getDate() + 7);

  const cards = document.querySelectorAll(".match-card[data-match-id]");
  let visibleTotal = 0;

  cards.forEach((card) => {
    const id = parseInt(card.dataset.matchId, 10);
    let show = true;

    if (q) {
      const haystack = [
        ...Array.from(card.querySelectorAll(".team-name")).map(
          (n) => n.textContent
        ),
        card.querySelector(".match-meta")?.textContent || "",
        `match ${id}`,
        `#${id}`,
      ]
        .join(" ")
        .toLowerCase();
      show = haystack.includes(q);
    }

    if (show && status) show = getCardStatusKey(card) === status;

    if (show && date) {
      const d = parseCardDate(card);
      if (!d) show = false;
      else if (date === "today") show = d.getTime() === today.getTime();
      else if (date === "week") show = d >= weekStart && d < weekEnd;
    }

    if (show && scorer) {
      const assigned = isScorerBlocking(scorerByMatch[id]);
      show = scorer === "assigned" ? assigned : !assigned;
    }

    card.style.display = show ? "" : "none";
    if (show) visibleTotal++;
  });

  document.querySelectorAll(".match-section").forEach((section) => {
    const anyVisible = Array.from(section.querySelectorAll(".match-card")).some(
      (c) => c.style.display !== "none"
    );
    section.style.display = anyVisible ? "" : "none";
  });

  /* "No results" message */
  const container = document.querySelector(".scrollable-content");
  let msg = document.getElementById("noResultsMsg");
  if (cards.length > 0 && visibleTotal === 0 && container) {
    if (!msg) {
      msg = el("p", "sa-no-results");
      msg.id = "noResultsMsg";
      container.appendChild(msg);
    }
    msg.textContent = q
      ? `No matches found for "${searchQuery.trim()}".`
      : "No matches found for the selected filters.";
  } else if (msg) {
    msg.remove();
  }
}

function initFilterBar() {
  const toggleBtn = document.getElementById("filterToggleBtn");
  const dropdown = document.getElementById("filterDropdown");
  const badge = document.getElementById("filterBadge");
  const statusSelect = document.getElementById("filterStatus");
  const dateSelect = document.getElementById("filterDate");
  const scorerSelect = document.getElementById("filterScorer");

  if (!toggleBtn || !dropdown) return;

  function openDropdown() {
    dropdown.hidden = false;
    toggleBtn.setAttribute("aria-expanded", "true");
  }

  function closeDropdown() {
    dropdown.hidden = true;
    toggleBtn.setAttribute("aria-expanded", "false");
  }

  function updateBadge() {
    const count = [statusSelect, dateSelect, scorerSelect].filter(
      (s) => s && s.value
    ).length;
    if (count > 0) {
      badge.hidden = false;
      badge.textContent = count;
      toggleBtn.classList.add("active");
    } else {
      badge.hidden = true;
      toggleBtn.classList.remove("active");
    }
  }

  bindOnce("filterToggleBtn", "click", (e) => {
    e.stopPropagation();
    dropdown.hidden ? openDropdown() : closeDropdown();
  });

  document.addEventListener("click", (e) => {
    if (
      !dropdown.hidden &&
      !dropdown.contains(e.target) &&
      e.target !== toggleBtn
    ) {
      closeDropdown();
    }
  });

  bindOnce("filterDropdown", "click", (e) => e.stopPropagation());

  bindOnce("filterApplyBtn", "click", () => {
    activeFilters = {
      status: statusSelect?.value || "",
      date: dateSelect?.value || "",
      scorer: scorerSelect?.value || "",
    };
    updateBadge();
    closeDropdown();
    applyCardFilters();
  });

  bindOnce("filterClearBtn", "click", () => {
    if (statusSelect) statusSelect.value = "";
    if (dateSelect) dateSelect.value = "";
    if (scorerSelect) scorerSelect.value = "";
    activeFilters = { status: "", date: "", scorer: "" };
    updateBadge();
    applyCardFilters();
  });
}

/* ---------------------------------------------------------
   8. SCORER STATE (load from server + apply to cards)
--------------------------------------------------------- */
function getTournamentIdFromUrl() {
  const m = window.location.pathname.match(/\/manage\/(\d+)/);
  return m ? m[1] : null;
}

/* A scorer blocks re-assigning while access is pending or live */
function isScorerBlocking(s) {
  return !!s && (s.status === "awaiting" || s.status === "active");
}

function normalizeStatus(value) {
  const v = String(value || "awaiting").toLowerCase();
  if (v.includes("revok")) return "revoked";
  if (v.includes("expir")) return "expired";
  if (v.includes("active") || v.includes("verified")) return "active";
  return "awaiting";
}

function normalizeScorer(raw) {
  return {
    matchId: raw.matchId,
    name: raw.scorerName ?? raw.name ?? "Scorer",
    email: raw.email ?? "",
    mobile: raw.mobileNumber ?? raw.mobile ?? "",
    duration:
      SA_DURATION_LABELS[raw.accessDuration] ??
      raw.accessDuration ??
      raw.duration ??
      "—",
    status: normalizeStatus(raw.status),
    otpVerified: !!raw.otpVerified,
    createdAt: raw.createdAt ?? null,
    expiresAt: raw.expiresAt ?? null,
  };
}

async function loadScorerAssignments() {
  const tournamentId = getTournamentIdFromUrl();
  if (!tournamentId) return;

  try {
    const res = await fetch(
      `/scorer/scorer-assignments?tournamentId=${tournamentId}`
    );
    if (!res.ok) throw new Error(`status ${res.status}`);
    const list = await res.json();

    Object.keys(scorerByMatch).forEach((k) => delete scorerByMatch[k]);
    list.forEach((raw) => {
      scorerByMatch[raw.matchId] = normalizeScorer(raw);
    });
  } catch (err) {
    console.warn(
      "scorer-assignments endpoint not available (" +
        err.message +
        "). Scorer state will reset when the page reloads until this endpoint exists."
    );
  }
  applyScorerState();
}

function applyScorerState() {
  document.querySelectorAll(".match-card[data-match-id]").forEach((card) => {
    const id = parseInt(card.dataset.matchId, 10);
    const s = scorerByMatch[id];
    const blocking = isScorerBlocking(s);
    const stateKey = blocking ? "active" : "none";

    const scorerBtn = card.querySelector(
      '[data-action="assign-scorer"], [data-action="manage-scorer"]'
    );
    const manageScoringBtn = card.querySelector(
      '[data-action="manage-scoring"]'
    );

    /* BUTTON 2 swaps between Assign Scorer and Manage Scorer */
    if (scorerBtn) {
      scorerBtn.disabled = false;
      scorerBtn.dataset.scorerState = stateKey;

      const label = scorerBtn.querySelector(".js-scorer-label") || scorerBtn;

      if (blocking) {
        scorerBtn.dataset.action = "manage-scorer";
        label.textContent = "Manage Scorer";
        scorerBtn.classList.add("sa-btn-scorer");
      } else {
        scorerBtn.dataset.action = "assign-scorer";
        label.textContent = "Assign Scorer";
        scorerBtn.classList.remove("sa-btn-scorer");
      }
    }

    /* BUTTON 1 (Manage Scoring) stays enabled; with a scorer it asks first */
    if (manageScoringBtn) {
      manageScoringBtn.disabled = false;
      manageScoringBtn.dataset.scorerState = stateKey;
      manageScoringBtn.title = blocking
        ? "A scorer is assigned. You'll be asked to confirm before taking over."
        : "You'll be asked to confirm before you start scoring.";
    }

    /* Scorer chip above the buttons (only while a scorer holds access) */
    let chip = card.querySelector(".sa-chip");
    if (blocking) {
      if (!chip) {
        chip = document.createElement("div");
        card.insertBefore(chip, card.querySelector(".card-actions"));
      }
      chip.className = `sa-chip sa-chip--${s.status}`;
      chip.innerHTML = `<span class="sa-chip-dot"></span>Scorer: <strong>${escapeHtml(
        s.name
      )}</strong> · ${SA_STATUS_LABELS[s.status]}`;
    } else if (chip) {
      chip.remove();
    }
  });

  applyCardFilters(); // keeps the Assigned/Unassigned filter correct
}

/* ---------------------------------------------------------
   9. ASSIGN / REPLACE SCORER MODAL + FORM
   The same modal is used for both. replaceMode switches the
   title, button text and API endpoint.
--------------------------------------------------------- */
function setAssignModalMode(isReplace) {
  replaceMode = isReplace;

  const title = document.getElementById("assignScorerTitle");
  const subtitle = document.getElementById("assignScorerSubtitle");
  const submitBtn = document.getElementById("assignScorerSubmitBtn");

  if (title) title.textContent = isReplace ? "Replace Scorer" : "Assign Scorer";
  if (subtitle) {
    subtitle.textContent = isReplace
      ? "The current scorer will lose access and the new scorer will receive an OTP."
      : "Give temporary scoring access for this match.";
  }
  if (submitBtn) {
    submitBtn.textContent = isReplace ? "Replace Scorer" : "Assign Scorer";
  }
}

function openAssignScorerModal(matchId, options = {}) {
  const match = getMatch(matchId);
  if (!match) return;
  activeMatchId = matchId;

  setAssignModalMode(!!options.replace);

  document.getElementById("assignModalMatchSummary").innerHTML = `
    <div class="mms-number">Match #${match.number}</div>
    <div class="mms-teams">
      <span class="mms-team">${escapeHtml(match.team1)}</span>
      <span class="mms-vs">VS</span>
      <span class="mms-team">${escapeHtml(match.team2)}</span>
    </div>
    <div class="mms-meta">
      <span>${escapeHtml(match.dateTime)}</span>
      <span>${escapeHtml(match.venue)}</span>
    </div>
  `;

  document.getElementById("assignScorerForm").reset();
  clearFormErrors();

  openModal("assignScorerOverlay");
}

function clearFormErrors() {
  ["scorerName", "scorerEmail", "scorerMobile", "accessDuration"].forEach(
    (id) => {
      document
        .getElementById(id)
        .closest(".field-group")
        .classList.remove("field-error-active");
      document.getElementById(
        "error" + id.charAt(0).toUpperCase() + id.slice(1)
      ).textContent = "";
    }
  );
}

function showFieldError(inputId, message) {
  const input = document.getElementById(inputId);
  input.closest(".field-group").classList.add("field-error-active");
  const errorId = "error" + inputId.charAt(0).toUpperCase() + inputId.slice(1);
  document.getElementById(errorId).textContent = message;
}

function validateScorerForm() {
  let isValid = true;
  clearFormErrors();

  const name = document.getElementById("scorerName").value.trim();
  const email = document.getElementById("scorerEmail").value.trim();
  const mobile = document.getElementById("scorerMobile").value.trim();
  const duration = document.getElementById("accessDuration").value;

  if (!name) {
    showFieldError("scorerName", "Scorer name is required.");
    isValid = false;
  } else if (name.length < 2) {
    showFieldError("scorerName", "Name must be at least 2 characters.");
    isValid = false;
  }

  if (!email) {
    showFieldError("scorerEmail", "Scorer email is required.");
    isValid = false;
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
    showFieldError("scorerEmail", "Enter a valid email address.");
    isValid = false;
  }

  if (!mobile) {
    showFieldError("scorerMobile", "Mobile number is required.");
    isValid = false;
  } else if (!/^[6-9]\d{9}$/.test(mobile)) {
    showFieldError(
      "scorerMobile",
      "Enter a valid 10-digit Indian mobile number."
    );
    isValid = false;
  }

  if (!duration) {
    showFieldError("accessDuration", "Access duration is required.");
    isValid = false;
  }

  return isValid;
}

async function assignScorer(e) {
  e.preventDefault();
  if (!validateScorerForm()) return;
  if (activeMatchId === null) return;

  const matchId = activeMatchId;
  const match = getMatch(matchId);
  if (!match) return;

  const isReplace = replaceMode; // lock the mode for this request
  const submitBtn = document.getElementById("assignScorerSubmitBtn");
  const name = document.getElementById("scorerName").value.trim();
  const email = document.getElementById("scorerEmail").value.trim();
  const mobile = document.getElementById("scorerMobile").value.trim();
  const duration = document.getElementById("accessDuration").value;

  const durationMap = {
    "Until Match Ends": "UNTIL_MATCH_ENDS",
    "2 Hours": "TWO_HOURS",
    "6 Hours": "SIX_HOURS",
    "24 Hours": "TWENTY_FOUR_HOURS",
  };

  const payLoad = {
    scorerName: name,
    email: email,
    mobileNumber: mobile,
    accessDuration: durationMap[duration] ?? duration,
  };

  const endpoint = isReplace
    ? `/scorer/${matchId}/replace-scorer`
    : `/scorer/${matchId}/assign-scorer`;

  try {
    if (submitBtn) {
      submitBtn.disabled = true;
      submitBtn.textContent = isReplace ? "Replacing...." : "Assigning....";
    }

    const res = await fetch(endpoint, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payLoad),
    });

    if (!res.ok) {
      const errText = await res.text().catch(() => "");
      throw new Error(errText || `Request failed with status ${res.status}`);
    }

    /* Use whatever the server returns, fall back to the form values */
    const created = await res.json().catch(() => null);
    const fromServer =
      created && typeof created === "object"
        ? Object.fromEntries(
            Object.entries(created).filter(
              ([, v]) => v !== null && v !== undefined
            )
          )
        : {};

    scorerByMatch[matchId] = normalizeScorer({
      ...payLoad,
      status: "awaiting",
      otpVerified: false,
      createdAt: new Date().toISOString(),
      ...fromServer,
      matchId: matchId,
    });

    closeModal(document.getElementById("assignScorerOverlay"));
    applyScorerState(); // stays on "Manage Scorer" with the new scorer
    logActivity(
      isReplace
        ? `Scorer replaced for Match #${matchId}`
        : `Scorer assigned to Match #${matchId}`
    );

    showSuccessPopup(
      isReplace
        ? "Scorer Replaced Successfully"
        : "Scorer Assigned Successfully",
      `${
        isReplace ? "The new scorer has been set" : "Scorer has been assigned"
      } for ${match.team1 || "Team A"} vs ${match.team2 || "Team B"}.`
    );

    loadScorerAssignments(); // sync with the server when it supports it
  } catch (err) {
    console.error("assignScorer failed:", err);
    showToast(
      "danger",
      isReplace ? "Could not replace scorer." : "Could not assign scorer.",
      "Please check the details and try again."
    );
  } finally {
    if (submitBtn) {
      submitBtn.disabled = false;
      submitBtn.textContent = isReplace ? "Replace Scorer" : "Assign Scorer";
    }
  }
}

/* ---------------------------------------------------------
   10. MANAGE SCORER MODAL  (#viewAccessOverlay in the HTML)
   Resend Access / Replace Scorer / Revoke Access
--------------------------------------------------------- */
function openManageScorerModal(matchId) {
  const s = scorerByMatch[matchId];
  if (!s) return;
  activeMatchId = matchId;

  const match = getMatch(matchId) || {};
  document.getElementById(
    "viewAccessMatchLine"
  ).textContent = `Match #${matchId} — ${match.team1 || "Team A"} vs ${
    match.team2 || "Team B"
  }`;

  document.getElementById("scorerSummary").innerHTML = `
    <span class="sa-avatar">${escapeHtml(initials(s.name))}</span>
    <div class="sa-person-text">
      <div class="sa-name">${escapeHtml(s.name)}</div>
      <div class="sa-sub">${escapeHtml(s.email || "No email on file")}</div>
    </div>
    ${scoringStatusMarkup(s)}`;

  const expires = s.expiresAt
    ? formatWhen(s.expiresAt)
    : s.duration === "Until Match Ends"
    ? "When the match ends"
    : "—";

  document.getElementById("accessDetailList").innerHTML = `
    <div><dt>Mobile</dt><dd>${
      s.mobile ? "+91 " + escapeHtml(maskMobile(s.mobile)) : "—"
    }</dd></div>
    <div><dt>OTP Verification</dt><dd>${
      s.otpVerified ? "Verified" : "Pending"
    }</dd></div>
    <div><dt>Access Duration</dt><dd>${escapeHtml(s.duration)}</dd></div>
    <div><dt>Assigned On</dt><dd>${escapeHtml(
      formatWhen(s.createdAt)
    )}</dd></div>
    <div class="detail-full"><dt>Access Expires</dt><dd>${escapeHtml(
      expires
    )}</dd></div>`;

  const notes = {
    awaiting:
      "The scorer has not verified their OTP yet. If they can't find the email, resend it.",
    active: "The scorer can update the live score for this match.",
    expired: "This scorer's access time has ended.",
    revoked: "This scorer can no longer update the score.",
  };
  const note = document.getElementById("scorerNote");
  note.className = `sa-note sa-note--${s.status}`;
  note.textContent = notes[s.status];

  /* This modal only opens for an awaiting/active scorer */
  document.getElementById("viewAccessActions").hidden = false;
  document.getElementById("viewAccessInactiveActions").hidden = true;

  openModal("viewAccessOverlay");
}

async function resendScorerAccess(matchId, btn) {
  const original = btn.textContent;
  try {
    btn.disabled = true;
    btn.textContent = "Sending...";
    const res = await fetch(`/scorer/${matchId}/resend-scorer-access`, {
      method: "POST",
    });
    if (!res.ok)
      throw new Error(
        (await res.text().catch(() => "")) || `status ${res.status}`
      );
    showToast(
      "success",
      "Access email sent.",
      "The scorer will receive it shortly."
    );
  } catch (err) {
    console.error("resendScorerAccess failed:", err);
    showToast(
      "danger",
      "Could not resend email.",
      "Please try again in a moment."
    );
  } finally {
    btn.disabled = false;
    btn.textContent = original;
  }
}

/* ---------------------------------------------------------
   11. REVOKE ACCESS (uses #revokeConfirmOverlay in your HTML)
--------------------------------------------------------- */
async function revokeScorerRequest(matchId, reason) {
  const res = await fetch(`/scorer/${matchId}/revoke-scorer`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ reason }),
  });
  if (!res.ok) {
    throw new Error(
      (await res.text().catch(() => "")) || `status ${res.status}`
    );
  }
  // The body is not needed. If the server sends no JSON, don't treat
  // a successful revoke as a failure.
  return await res.json().catch(() => null);
}

function requestRevoke(matchId) {
  const s = scorerByMatch[matchId];
  if (!s) return;
  activeMatchId = matchId;

  document.getElementById(
    "revokeConfirmBody"
  ).textContent = `${s.name} will no longer be able to update the score for this match.`;

  openModal("revokeConfirmOverlay");
}

async function revokeAccess() {
  if (activeMatchId === null) return;
  const matchId = activeMatchId;
  const s = scorerByMatch[matchId];
  if (!s) return;

  const btn = document.getElementById("revokeConfirmBtn");
  try {
    if (btn) btn.disabled = true;

    await revokeScorerRequest(matchId, "ORGANIZER_REVOKED");

    s.status = "revoked";
    closeAllModals();
    applyScorerState(); // Manage Scorer -> Assign Scorer
    showToast(
      "danger",
      "Scoring access revoked.",
      `${escapeHtml(s.name)} can no longer update this match.`
    );
    logActivity(`Scoring access revoked for Match #${matchId}`);
  } catch (err) {
    console.error("revokeAccess failed:", err);
    showToast("danger", "Could not revoke access.", "Please try again.");
  } finally {
    if (btn) btn.disabled = false;
  }
}

/* ---------------------------------------------------------
   12. MANAGE SCORING + TAKEOVER (#takeoverConfirmOverlay)
   Every click on Manage Scoring opens the confirmation popup.
   No scorer     -> OK -> call managing-score
   Active scorer -> OK -> revoke scorer -> call managing-score
--------------------------------------------------------- */
function handleManageScoring(id) {
  openTakeoverModal(id); // always ask first
}

function openTakeoverModal(matchId) {
  activeMatchId = matchId;
  const s = scorerByMatch[matchId]; // may be undefined (no scorer assigned)

  // Show the teams (e.g. "real Madrid vs Mumbai warriors") instead of the match number,
  // highlighted in colour so the organizer can spot them quickly.
  const match = getMatch(matchId);
  const matchLabel =
    match && match.team1 && match.team2
      ? `${escapeHtml(match.team1)} vs ${escapeHtml(match.team2)}`
      : `Match #${escapeHtml(matchId)}`; // fallback if team names can't be read

  // Change this colour (amber, same as the Take Over button) to restyle the names
  const hl = (html) =>
    `<strong style="color:#f59e0b;font-weight:700;">${html}</strong>`;

  const body = document.getElementById("takeoverConfirmBody");
  if (body) {
    body.innerHTML = isScorerBlocking(s)
      ? `<strong style="color:#f2f2f5;">${escapeHtml(
          s.name
        )}</strong> currently has scoring access for ${hl(matchLabel)}. ` +
        `If you continue, their access will be revoked right away and only you ` +
        `will be able to manage the score. Do you really want to take over?`
      : `You are about to manage the score of ${hl(matchLabel)} yourself. ` +
        `Once you continue, no one else will be able to update the score for ` +
        `this match. Do you really want to continue?`;
  }

  openModal("takeoverConfirmOverlay");
}
async function confirmTakeover() {
  const matchId = activeMatchId;
  // CHANGED (1): was `if (matchId === null) return;` which let NaN through
  if (!Number.isFinite(matchId)) {
    console.error("confirmTakeover: invalid activeMatchId", matchId);
    return;
  }
  const s = scorerByMatch[matchId];

  const btn = document.getElementById("takeoverConfirmBtn");
  const original = btn.textContent;
  try {
    btn.disabled = true;
    btn.textContent = "Switching...";

    if (isScorerBlocking(s)) {
      await revokeScorerRequest(matchId, "ORGANIZER_TAKEOVER");
      s.status = "revoked";
    }

    closeAllModals();
    applyScorerState(); // Assign Scorer is available again
    logActivity(`Organizer took over scoring for Match #${matchId}`);
    await openManagingScoring(matchId);
  } catch (err) {
    console.error("confirmTakeover failed:", err);
    showToast(
      "danger",
      "Could not switch to organizer scoring.",
      "The scorer's access was not changed. Please try again."
    );
  } finally {
    btn.disabled = false;
    btn.textContent = original;
  }
}

/* ---------------------------------------------------------
   13. MATCH DETAILS MODAL
--------------------------------------------------------- */
function openMatchDetails(matchId) {
  const match = getMatch(matchId);
  if (!match) return;

  const statusLabelMap = {
    upcoming: "Scheduled",
    live: "Live",
    completed: "Completed",
  };

  const scorer = scorerByMatch[matchId];
  const scorerLine = isScorerBlocking(scorer)
    ? `${scorer.name} (${SA_STATUS_LABELS[scorer.status]})`
    : "Organizer (you)";

  document.getElementById("matchDetailsList").innerHTML = `
    <div><dt>Match</dt><dd>#${match.number}</dd></div>
    <div><dt>Status</dt><dd>${statusLabelMap[match.status] || "—"}</dd></div>
    <div class="detail-full"><dt>Teams</dt><dd>${escapeHtml(
      match.team1
    )} vs ${escapeHtml(match.team2)}</dd></div>
    <div><dt>Date &amp; Time</dt><dd>${escapeHtml(match.dateTime)}</dd></div>
    <div><dt>Venue</dt><dd>${escapeHtml(match.venue)}</dd></div>
    <div><dt>Scoring By</dt><dd>${escapeHtml(scorerLine)}</dd></div>
  `;

  openModal("matchDetailsOverlay");
}

/* ---------------------------------------------------------
   14. MANAGING SCORING PAGE  (fragment from /scorer/managing-score)
   Calls the API, injects the returned HTML next to #mainContent,
   hides the match list, and starts the scorekeeper UI.
--------------------------------------------------------- */
const ARROW_LEFT =
  '<svg width="15" height="15" viewBox="0 0 24 24" fill="none"><path d="m15 18-6-6 6-6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>';

/* Load the scorekeeper script once (scripts inside injected HTML never run) */
function ensureScorekeeperScript() {
  if (typeof window.initScorekeeper === "function") return Promise.resolve();

  return new Promise((resolve, reject) => {
    const s = document.createElement("script");
    s.src = SCOREKEEPER_JS;
    s.onload = () => resolve();
    s.onerror = () => reject(new Error("Could not load " + SCOREKEEPER_JS));
    document.head.appendChild(s);
  });
}
function loadScriptOnce(src) {
  if (scriptPromises[src]) return scriptPromises[src];

  scriptPromises[src] = new Promise((resolve, reject) => {
    const s = document.createElement("script");
    s.src = src;
    s.onload = resolve;
    s.onerror = () => {
      delete scriptPromises[src];
      reject(new Error("Could not load " + src));
    };
    document.body.appendChild(s);
  });

  return scriptPromises[src];
}
async function openManagingScoring(matchId) {
  if (scoringBusy) return;

  // CHANGED (2): validate first, before setting any state or calling the API
  matchId = Number(matchId);
  if (!Number.isFinite(matchId)) {
    console.error("openManagingScoring: invalid matchId", matchId);
    showToast(
      "danger",
      "Could not open scoring.",
      "Invalid match. Please refresh and try again."
    );
    return;
  }

  const main = document.getElementById("mainContent");
  if (!main) return;

  scoringBusy = true;
  activeMatchId = matchId;

  try {
    // 1. call the managing-score API
    const res = await fetch(`${MANAGING_SCORE_URL}?matchId=${matchId}`);
    if (!res.ok) throw new Error(`status ${res.status}`);
    const html = await res.text();

    // 2. put the returned fragment on the page
    let host = document.getElementById("managingScoringHost");
    if (!host) {
      host = el("section", "scoring-page");
      host.id = "managingScoringHost";
      main.insertAdjacentElement("afterend", host);
    }
    host.innerHTML =
      `<button type="button" class="crumb-back" id="backFromManagingScoring">${ARROW_LEFT} Back to Live Scoring</button>` +
      `<div id="managingScoringBody"></div>`;
    document.getElementById("managingScoringBody").innerHTML = html;

    document
      .getElementById("backFromManagingScoring")
      .addEventListener("click", backToLiveScoring);

    // 3. swap views
    main.hidden = true;
    toggleTournamentBackLink(true);
    host.hidden = false;
    window.scrollTo({ top: 0, behavior: "smooth" });

    // 4. load WebSocket libraries, then the scorekeeper UI
    await loadScriptOnce(
      "https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js"
    );
    await loadScriptOnce(
      "https://cdn.jsdelivr.net/npm/stompjs@2.3.3/lib/stomp.min.js"
    );
    await loadScriptOnce("/js/fragments/Managing_Scoring/cricket-websocket.js");

    await ensureScorekeeperScript();

    // ===== pass matchId + match name to the scorekeeper =====
    const rootEl = document.getElementById("scorekeeperRoot");
    const m = getMatch(matchId);
    if (rootEl) {
      rootEl.dataset.matchId = matchId;
      if (m && m.team1 && m.team2) {
        rootEl.dataset.matchName = m.team1 + " vs " + m.team2;
      }
    }
    // ===== END =====

    window.initScorekeeper();
  } catch (err) {
    console.error("openManagingScoring failed:", err);
    backToLiveScoring();
    showToast(
      "danger",
      "Could not open scoring.",
      "Please try again in a moment."
    );
  } finally {
    scoringBusy = false;
  }
}
function backToLiveScoring() {
  if (typeof window.destroyScorekeeper === "function") {
    window.destroyScorekeeper();
  }
  document.getElementById("managingScoringHost")?.remove();

  const main = document.getElementById("mainContent");
  if (main) main.hidden = false;
  toggleTournamentBackLink(false);

  window.scrollTo({ top: 0, behavior: "smooth" });
}

/* ---------------------------------------------------------
   15. EVENT DELEGATION FOR MATCH CARD ACTIONS
--------------------------------------------------------- */
document.addEventListener("click", (e) => {
  const trigger = e.target.closest("[data-action]");
  if (!trigger || trigger.disabled) return;

  const action = trigger.dataset.action;
  const id = parseInt(trigger.dataset.id ?? trigger.dataset.matchId, 10);

  // CHANGED (3): stop NaN at the source and log which button has a bad data-id
  if (Number.isNaN(id)) {
    console.error(
      `Button with data-action="${action}" has missing/invalid data-id:`,
      trigger
    );
    return;
  }

  switch (action) {
    case "manage-scoring":
      handleManageScoring(id);
      break;
    case "assign-scorer":
      openAssignScorerModal(id);
      break;
    case "manage-scorer":
      openManageScorerModal(id);
      break;
    case "view-details":
      openMatchDetails(id);
      break;
  }
});

/* ---------------------------------------------------------
   16. INIT
   manageTournament.js calls initLiveScoring() right after it
   injects this page's HTML.
--------------------------------------------------------- */
function initLiveScoring() {
  bindOnce("assignScorerForm", "submit", assignScorer);
  bindOnce("revokeConfirmBtn", "click", revokeAccess);
  bindOnce("takeoverConfirmBtn", "click", confirmTakeover);

  /* Manage Scorer modal buttons */
  bindOnce("resendAccessBtn", "click", (e) => {
    resendScorerAccess(activeMatchId, e.currentTarget);
  });

  bindOnce("replaceScorerBtn", "click", () => {
    const matchId = activeMatchId;
    closeModal(document.getElementById("viewAccessOverlay"));
    openAssignScorerModal(matchId, { replace: true });
  });

  bindOnce("revokeAccessTrigger", "click", () => {
    const matchId = activeMatchId;
    closeModal(document.getElementById("viewAccessOverlay"));
    requestRevoke(matchId);
  });

  /* Restrict mobile number input to digits only */
  bindOnce("scorerMobile", "input", (e) => {
    e.target.value = e.target.value.replace(/\D/g, "").slice(0, 10);
  });

  /* Search */
  const searchInput = document.getElementById("matchSearchInput");
  if (searchInput) {
    searchInput.value = searchQuery;
    bindOnce("matchSearchInput", "input", (e) => {
      searchQuery = e.target.value;
      applyCardFilters();
    });
  }

  initFilterBar();
  renderActivity();

  applyScorerState(); // show cards in their default state right away
  loadScorerAssignments(); // then sync scorer data from the server
}
