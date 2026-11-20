/* =========================================================
   CHAMPIONCALL SCOREKEEPER — merged + updated self-contained script
   ========================================================= */
(function () {
  "use strict";

  var SPORTS = ["cricket", "kabaddi", "football"];
  var root = null;
  var state = null;
  var eventIdCounter = 1;
  var tickTimer = null;
  var queuedResetTimer = null;

  /* ---------------- Utilities ---------------- */
  function escapeHtml(value) {
    return String(value == null ? "" : value).replace(/[&<>"']/g, function (c) {
      return {
        "&": "&amp;",
        "<": "&lt;",
        ">": "&gt;",
        '"': "&quot;",
        "'": "&#39;",
      }[c];
    });
  }
  function clone(o) {
    return JSON.parse(JSON.stringify(o));
  }
  function pad2(n) {
    return String(n).padStart(2, "0");
  }
  function nowLabel() {
    var d = new Date();
    return pad2(d.getHours()) + ":" + pad2(d.getMinutes());
  }
  function ov(balls) {
    return Math.floor(balls / 6) + "." + (balls % 6);
  }
  function clockLabel(sec) {
    return pad2(Math.floor(sec / 60)) + ":" + pad2(sec % 60);
  }
  function plural(n, word) {
    return n + " " + word + (n === 1 ? "" : "s");
  }

  function icon(name, size) {
    size = size || 14;
    var paths = {
      wifi: '<path d="M5 12.55a11 11 0 0 1 14 0"/><path d="M1.42 9a16 16 0 0 1 21.16 0"/><path d="M8.53 16.11a6 6 0 0 1 6.95 0"/><line x1="12" y1="20" x2="12.01" y2="20"/>',
      wifiOff:
        '<line x1="1" y1="1" x2="23" y2="23"/><path d="M16.72 11.06A10.94 10.94 0 0 1 19 12.55"/><path d="M5 12.55a10.94 10.94 0 0 1 5.17-2.39"/><path d="M10.71 5.05A16 16 0 0 1 22.58 9"/><path d="M1.42 9a15.91 15.91 0 0 1 4.7-2.88"/><path d="M8.53 16.11a6 6 0 0 1 6.95 0"/><line x1="12" y1="20" x2="12.01" y2="20"/>',
      lock: '<rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/>',
      undo: '<polyline points="1 4 1 10 7 10"/><path d="M3.51 15a9 9 0 1 0 2.13-9.36L1 10"/>',
      alert:
        '<path d="M10.29 3.86 1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/>',
      play: '<polygon points="5 3 19 12 5 21 5 3"/>',
      pause:
        '<rect x="6" y="4" width="4" height="16"/><rect x="14" y="4" width="4" height="16"/>',
    };
    return (
      '<svg class="cc-icon" width="' +
      size +
      '" height="' +
      size +
      '" viewBox="0 0 24 24" aria-hidden="true">' +
      (paths[name] || "") +
      "</svg>"
    );
  }

  /* ---------------- Initial data ---------------- */
  function initialCricket() {
    return {
      round: "Quarter final",
      innings: 2,
      teamA: { name: "Mumbai Titans", short: "MUT", score: 121, wickets: 4 },
      teamB: {
        name: "Chennai Kings",
        short: "CHK",
        score: 136,
        wickets: 8,
        overs: "20.0",
      },
      target: 137,
      oversLimit: 20,
      legalBalls: 105,
      striker: { name: "R. Sharma", runs: 58, balls: 42 },
      nonStriker: { name: "S. Iyer", runs: 24, balls: 18 },
      bowler: { name: "J. Bumrah", balls: 23, runs: 22, wickets: 2 },
      bowlers: {},
      lastBowler: "",
      currentOverBalls: [
        { label: "1", type: "run" },
        { label: "0", type: "run" },
        { label: "4", type: "run" },
      ],
      previousOvers: [
        { overNum: 16, balls: ["1", "4", "0", "0", "W", "2"] },
        { overNum: 17, balls: ["0", "1", "4", "6", "1", "2"] },
      ],
      freeHit: false,
      awaitingBatter: false,
      awaitingSlot: null,
      awaitingBowler: false,
      result: "",
      extras: { wide: 0, noBall: 0, bye: 0, legBye: 0 },
    };
  }
  function initialKabaddi() {
    return {
      round: "Semi final",
      half: 2,
      halfLengthSeconds: 1200,
      timeRemainingSeconds: 754,
      running: true,
      teamA: { name: "Bengal Warriors", short: "BEN", score: 18, timeouts: 1 },
      teamB: { name: "Patna Pirates", short: "PAT", score: 15, timeouts: 2 },
      raidingTeam: "A",
      raidNumber: 23,
      raidTimeLimit: 30,
      raidTimeLeft: 30,
      raidTimeUp: false,
      raidTimerLocked: false,
      raidTimerPaused: false,
      raider: "P. Narwal",
      raiderId: 1,
      raiderIdx: { A: 1, B: 0 },
      outSeq: 3,
      doOrDie: false,
      result: "",
    };
  }
  function initialFootball() {
    return {
      round: "Group stage",
      home: { name: "FC Union", short: "UNI", score: 2 },
      away: { name: "Athletic Rovers", short: "ATR", score: 1 },
      half: 1,
      seconds: 45 * 60 + 32,
      running: true,
      addedTime: 2,
      selectedSide: "home",
      cards: {},
      result: "",
      events: [
        { time: "45:32", type: "Goal", team: "UNI" },
        { time: "38:12", type: "Yellow card", team: "ATR" },
        { time: "31:44", type: "Substitution", team: "UNI" },
        { time: "17:20", type: "Goal", team: "ATR" },
      ],
    };
  }
  function withSquads(s, squads) {
    s.squads = squads;
    return s;
  }
  function pl(list) {
    return list.map(function (r, i) {
      return { id: i + 1, name: r[0], role: r[1] || "", status: r[2] || "yet" };
    });
  }
  function cricketSquads() {
    return {
      A: {
        name: "Mumbai Titans",
        short: "MUT",
        color: "#1e5bff",
        players: pl([
          ["R. Sharma", "C", "bat", 58, 42, "not out"],
          ["S. Iyer", "", "bat", 24, 18, "not out"],
          ["S. Gill", "VC", "out", 14, 16, "c Conway b Chahar"],
          ["V. Kohli", "", "out", 9, 12, "lbw b Jadeja"],
          ["K. Rahul", "WK", "out", 6, 9, "b Pathirana"],
          ["H. Pandya", "", "out", 10, 8, "c Dube b Bumrah"],
          ["W. Sundar"],
          ["K. Yadav"],
          ["A. Singh"],
          ["P. Krishna"],
          ["M. Siraj"],
        ]),
      },
      B: {
        name: "Chennai Kings",
        short: "CHK",
        color: "#f5b50a",
        players: pl([
          ["R. Gaikwad", "C"],
          ["D. Conway", "WK"],
          ["S. Dube"],
          ["R. Jadeja", "VC"],
          ["M. Ali"],
          ["J. Bumrah"],
          ["D. Chahar"],
          ["T. Deshpande"],
          ["M. Pathirana"],
          ["S. Rashid"],
          ["M. Theekshana"],
        ]),
      },
    };
  }
  function kbTeam(name, short, color, rows) {
    return {
      name: name,
      short: short,
      color: color,
      players: rows.map(function (r, i) {
        return {
          id: i + 1,
          name: r[0],
          role: r[1],
          on: r[2] !== 0,
          out: false,
          outSeq: 0,
        };
      }),
    };
  }
  function kabaddiSquads() {
    var sq = {
      A: kbTeam("Bengal Warriors", "BEN", "#16a34a", [
        ["P. Narwal", "Raider"],
        ["M. Singh", "Raider"],
        ["S. Kumar", "All-rounder"],
        ["R. Das", "Defender"],
        ["A. Sheikh", "Defender"],
        ["V. Roy", "Defender"],
        ["K. Paul", "Defender"],
        ["D. Ghosh", "Raider", 0],
        ["T. Sen", "Defender", 0],
      ]),
      B: kbTeam("Patna Pirates", "PAT", "#7c3aed", [
        ["S. Goyat", "Raider"],
        ["N. Kumar", "Raider"],
        ["A. Yadav", "All-rounder"],
        ["M. Khan", "Defender"],
        ["R. Jha", "Defender"],
        ["V. Mishra", "Raider"],
        ["D. Singh", "Defender"],
        ["P. Thakur", "Defender", 0],
      ]),
    };
    // starting position: some players already out (order = revival order)
    function markOut(L, idx, seq) {
      var p = sq[L].players[idx];
      p.out = true;
      p.outSeq = seq;
    }
    markOut("A", 6, 1);
    markOut("B", 5, 2);
    markOut("B", 6, 3);
    return sq;
  }
  function fbTeam(name, short, color, rows) {
    return {
      name: name,
      short: short,
      color: color,
      players: rows.map(function (r, i) {
        return {
          id: i + 1,
          name: r[0],
          pos: r[1],
          no: r[2],
          on: r[3] !== 0,
          sentOff: false,
        };
      }),
    };
  }
  function footballSquads() {
    return {
      A: fbTeam("FC Union", "UNI", "#dc2626", [
        ["Keller", "GK", 1],
        ["Brandt", "DEF", 2],
        ["Vogel", "DEF", 4],
        ["Hahn", "DEF", 5],
        ["Roth", "DEF", 3],
        ["Meyer", "MID", 8],
        ["Lang", "MID", 6],
        ["Beck", "MID", 10],
        ["Kraus", "FWD", 7],
        ["Wolf", "FWD", 9],
        ["Stein", "FWD", 11],
        ["Fuchs", "SUB", 12, 0],
        ["Pohl", "SUB", 14, 0],
        ["Arnold", "SUB", 20, 0],
      ]),
      B: fbTeam("Athletic Rovers", "ATR", "#0ea5e9", [
        ["Hall", "GK", 1],
        ["Cole", "DEF", 2],
        ["Reid", "DEF", 5],
        ["Shaw", "DEF", 4],
        ["Dunn", "DEF", 3],
        ["Webb", "MID", 8],
        ["Page", "MID", 6],
        ["Hunt", "MID", 10],
        ["Cross", "FWD", 7],
        ["Ford", "FWD", 9],
        ["Lowe", "FWD", 11],
        ["Gray", "SUB", 12, 0],
        ["Marsh", "SUB", 14, 0],
      ]),
    };
  }
  function createState() {
    return {
      matchId: (root && root.dataset.matchId) || "",
      matchName: (root && root.dataset.matchName) || "",
      scorekeeper: (root && root.dataset.scorekeeper) || "",
      sport: "cricket",
      status: "live",
      online: typeof navigator !== "undefined" ? navigator.onLine : true,
      queuedCount: 0,
      cricket: withSquads(initialCricket(), cricketSquads()),
      kabaddi: withSquads(initialKabaddi(), kabaddiSquads()),
      football: withSquads(initialFootball(), footballSquads()),
      history: [],
      eventLog: [],
      pendingConfirm: null,
      picker: null,
    };
  }

  /* ---------------- Log / history ---------------- */
  function logEvent(label, detail, highlight) {
    state.eventLog.unshift({
      id: eventIdCounter++,
      time: nowLabel(),
      label: label,
      detail: detail,
      highlight: !!highlight,
    });
    if (state.eventLog.length > 40) state.eventLog.length = 40;
    if (!state.online) state.queuedCount++;
  }
  function pushHistory(sportName) {
    state.history.push({
      sport: sportName,
      state: clone(state[sportName]),
      status: state.status,
      eventLog: clone(state.eventLog),
      queuedCount: state.queuedCount,
    });
    if (state.history.length > 25) state.history.shift();
  }
  function notice(title, body) {
    state.pendingConfirm = {
      title: title,
      body: body,
      confirmLabel: "OK",
      danger: false,
      kind: "notice",
    };
    render();
  }

  /* =========================================================
     CRICKET
     ========================================================= */
  function swapStrike(c) {
    var x = c.striker;
    c.striker = c.nonStriker;
    c.nonStriker = x;
  }
  function dismissalText(how, bowler, fielder) {
    var f = fielder || "";
    if (how === "Caught") return (f ? "c " + f : "caught") + " b " + bowler;
    if (how === "Stumped") return "st " + (f ? f + " " : "") + "b " + bowler;
    if (how === "Run out") return "run out" + (f ? " (" + f + ")" : "");
    if (how === "LBW") return "lbw b " + bowler;
    if (how === "Hit wicket") return "hit wicket b " + bowler;
    return "b " + bowler;
  }
  // copies live striker / non-striker runs into the squad table
  function syncBatters(c) {
    [c.striker, c.nonStriker].forEach(function (b) {
      c.squads.A.players.forEach(function (p) {
        if (p.name === b.name && p.status === "bat") {
          p.runs = b.runs;
          p.balls = b.balls;
          p.how = "not out";
        }
      });
    });
  }
  function fielderPicker(how) {
    var c = state.cricket;
    var opts = c.squads.B.players.map(function (p) {
      return { label: p.name, value: p.name };
    });
    opts.push({ label: "Skip", value: "" });
    return {
      kind: "fielder",
      how: how,
      title: how + " — fielder",
      body: "Who took the catch / run out / stumping?",
      options: opts,
    };
  }

  function legalBall(next, label, type) {
    next.legalBalls++;
    next.bowler.balls++;
    next.currentOverBalls.push({ label: label, type: type || "run" });
    if (next.legalBalls % 6 === 0) {
      next.previousOvers.push({
        overNum: Math.floor(next.legalBalls / 6),
        balls: next.currentOverBalls.map(function (b) {
          return b.label;
        }),
      });
      next.currentOverBalls = [];
      swapStrike(next);
      next.lastBowler = next.bowler.name;
      if (next.legalBalls < next.oversLimit * 6) next.awaitingBowler = true;
    }
  }

  function dismissalPicker() {
    return {
      kind: "dismissal",
      title: "How was the batter out?",
      body: "Select the dismissal type.",
      options: [
        "Bowled",
        "Caught",
        "LBW",
        "Run out",
        "Stumped",
        "Hit wicket",
      ].map(function (h) {
        return { label: h, value: h };
      }),
    };
  }
  function extraPicker(type) {
    var cfg = {
      WIDE: {
        title: "Wide",
        body: "Extra runs run in addition to the 1 wide.",
        vals: [0, 1, 2, 3, 4],
      },
      NO_BALL: {
        title: "No ball",
        body: "Runs scored off the bat (plus 1 no ball).",
        vals: [0, 1, 2, 3, 4, 6],
      },
      BYE: {
        title: "Byes",
        body: "How many byes were run?",
        vals: [1, 2, 3, 4],
      },
      LEG_BYE: {
        title: "Leg byes",
        body: "How many leg byes were run?",
        vals: [1, 2, 3, 4],
      },
    }[type];
    return {
      kind: "extra",
      extraType: type,
      title: cfg.title,
      body: cfg.body,
      options: cfg.vals.map(function (v) {
        return { label: String(v), value: v };
      }),
    };
  }
  function bowlerPicker() {
    var c = state.cricket,
      maxBalls = Math.ceil(c.oversLimit / 5) * 6;
    var opts = c.squads.B.players
      .filter(function (p) {
        if (p.name === c.lastBowler) return false;
        var st = c.bowlers[p.name];
        return !(st && st.balls >= maxBalls);
      })
      .map(function (p) {
        var st = c.bowlers[p.name];
        return {
          label: p.name + (st ? " (" + ov(st.balls) + " ov)" : ""),
          value: p.name,
        };
      });
    return {
      kind: "bowler",
      title: "Select next bowler",
      body: "Over complete. The same bowler cannot bowl two overs in a row.",
      options: opts,
    };
  }
  function setBowler(name) {
    var c = state.cricket;
    if (!c.awaitingBowler) return;
    pushHistory("cricket");
    c.bowlers[c.bowler.name] = {
      balls: c.bowler.balls,
      runs: c.bowler.runs,
      wickets: c.bowler.wickets,
    };
    var st = c.bowlers[name] || { balls: 0, runs: 0, wickets: 0 };
    c.bowler = {
      name: name,
      balls: st.balls,
      runs: st.runs,
      wickets: st.wickets,
    };
    c.awaitingBowler = false;
    logEvent("New bowler", name + " to bowl", true);
    render();
  }

  function checkCricketResult() {
    var c = state.cricket;
    if (c.result) return;
    var a = c.teamA,
      b = c.teamB,
      res = "";
    if (a.score >= c.target) {
      var wl = 10 - a.wickets;
      res = a.name + " won by " + plural(wl, "wicket");
    } else if (a.wickets >= 10 || c.legalBalls >= c.oversLimit * 6) {
      if (a.score === c.target - 1) res = "Match tied";
      else res = b.name + " won by " + plural(c.target - 1 - a.score, "run");
    }
    if (res) {
      c.result = res;
      c.awaitingBowler = false;
      c.awaitingBatter = false;
      state.status = "completed";
      logEvent("Result", res, true);
    }
  }

  function cricketDispatch(action) {
    var c = state.cricket;
    if (action.type === "UNDO_CONFIRM") {
      handleUndoConfirm();
      return;
    }
    if (c.result) {
      notice("Match completed", c.result);
      return;
    }
    if (c.awaitingBatter) {
      notice(
        "Select next batter",
        "Choose the next batter from SQUADS before recording another ball."
      );
      return;
    }
    if (c.awaitingBowler) {
      state.picker = bowlerPicker();
      render();
      return;
    }
    if (action.type === "WICKET_CONFIRM") {
      state.pendingConfirm = {
        title: "Confirm wicket",
        body:
          "Record a wicket for " +
          c.striker.name +
          "? This ends the batter's innings.",
        confirmLabel: "Confirm wicket",
        danger: true,
        kind: "cricket-wicket",
      };
      render();
      return;
    }
    if (
      action.type === "WIDE" ||
      action.type === "NO_BALL" ||
      action.type === "BYE" ||
      action.type === "LEG_BYE"
    ) {
      state.picker = extraPicker(action.type);
      render();
      return;
    }
    applyCricket(action);
  }

  function applyCricket(action) {
    var prev = state.cricket;
    if (prev.awaitingBatter || prev.awaitingBowler || prev.result) return;
    pushHistory("cricket");
    var next = clone(prev),
      v = Number(action.value) || 0,
      legal = true,
      t = action.type;

    if (t === "RUN") {
      next.teamA.score += v;
      next.striker.runs += v;
      next.striker.balls++;
      next.bowler.runs += v;
      legalBall(next, String(v), "run");
      if (v % 2 === 1) swapStrike(next);
      logEvent(
        v === 4 || v === 6 ? "Boundary" : v === 0 ? "Dot ball" : "Run",
        plural(v, "run") + " — " + prev.striker.name,
        v === 4 || v === 6
      );
    } else if (t === "WICKET") {
      var how = action.how || "Bowled",
        outName = prev.striker.name;
      var outRuns = prev.striker.runs,
        outBalls = prev.striker.balls + 1;
      next.teamA.wickets++;
      next.striker.balls++;
      if (how !== "Run out") next.bowler.wickets++;
      legalBall(next, "W", "wicket");
      next.squads.A.players.forEach(function (p) {
        if (p.name === outName) {
          p.status = "out";
          p.runs = outRuns;
          p.balls = outBalls;
          p.how = dismissalText(how, prev.bowler.name, action.fielder);
        }
      });
      if (next.teamA.wickets < 10) {
        var slot = next.striker.name === outName ? "striker" : "nonStriker";
        var queued = next.squads.A.players.filter(function (p) {
          return p.status === "next";
        })[0];
        if (queued) {
          queued.status = "bat";
          next[slot] = { name: queued.name, runs: 0, balls: 0 };
          next.awaitingBatter = false;
          next.awaitingSlot = null;
        } else {
          next[slot] = { name: "New batter", runs: 0, balls: 0 };
          next.awaitingBatter = true;
          next.awaitingSlot = slot;
        }
      }
      logEvent(
        "Wicket",
        outName + " out (" + how + ") b " + prev.bowler.name,
        true
      );
    } else if (t === "WIDE") {
      var wr = 1 + v;
      next.teamA.score += wr;
      next.bowler.runs += wr;
      next.extras.wide += wr;
      next.currentOverBalls.push({
        label: v ? "Wd+" + v : "Wd",
        type: "extra",
      });
      legal = false;
      if (v % 2 === 1) swapStrike(next);
      logEvent("Wide", "+" + plural(wr, "run") + " — extra");
    } else if (t === "NO_BALL") {
      var nr = 1 + v;
      next.teamA.score += nr;
      next.bowler.runs += nr;
      next.extras.noBall += 1;
      next.striker.runs += v;
      next.striker.balls++;
      next.freeHit = true;
      legal = false;
      next.currentOverBalls.push({
        label: v ? "Nb+" + v : "Nb",
        type: "extra",
      });
      if (v % 2 === 1) swapStrike(next);
      logEvent(
        "No ball",
        "+" + plural(nr, "run") + " — free hit next legal ball"
      );
    } else if (t === "BYE" || t === "LEG_BYE") {
      var br = v || 1;
      next.teamA.score += br;
      next.striker.balls++;
      next.extras[t === "BYE" ? "bye" : "legBye"] += br;
      legalBall(next, (t === "BYE" ? "B" : "Lb") + (br > 1 ? br : ""), "extra");
      if (br % 2 === 1) swapStrike(next);
      logEvent(
        t === "BYE" ? "Bye" : "Leg bye",
        "+" + plural(br, "run") + " — extra"
      );
    } else if (t === "SWAP_STRIKE") {
      swapStrike(next);
      legal = false;
      logEvent("Strike swapped", next.striker.name + " on strike");
    } else {
      state.history.pop();
      return;
    }

    if (legal && prev.freeHit) next.freeHit = false;
    syncBatters(next);
    state.cricket = next;
    checkCricketResult();
    render();

    var c = state.cricket;
    if (state.status === "live" && !c.result) {
      if (c.awaitingBatter) openSquads("cricket");
      else if (c.awaitingBowler) {
        state.picker = bowlerPicker();
        render();
      }
    }
  }

  /* =========================================================
     KABADDI
     ========================================================= */
  /* ---- helpers (team keys are "A" / "B") ---- */
  function otherL(L) {
    return L === "A" ? "B" : "A";
  }
  function kbTeamObj(k, L) {
    return L === "A" ? k.teamA : k.teamB;
  }
  function kbFindPlayer(k, L, id) {
    return (
      k.squads[L].players.filter(function (p) {
        return p.id === id;
      })[0] || null
    );
  }
  function onMatPlayers(k, L) {
    return k.squads[L].players.filter(function (p) {
      return p.on && !p.out;
    });
  }
  function activeCount(k, L) {
    return onMatPlayers(k, L).length;
  }
  function setRaider(k, p) {
    k.raiderId = p ? p.id : null;
    k.raider = p ? p.name : "—";
  }
  // players go out; remembers the order so the first one out is the first one revived
  function setOutIds(k, L, ids) {
    var names = [];
    ids.forEach(function (id) {
      var p = kbFindPlayer(k, L, id);
      if (p && p.on && !p.out) {
        p.out = true;
        k.outSeq++;
        p.outSeq = k.outSeq;
        names.push(p.name);
      }
    });
    return names;
  }
  function reviveN(k, L, n) {
    var outs = k.squads[L].players
      .filter(function (p) {
        return p.on && p.out;
      })
      .sort(function (a, b) {
        return a.outSeq - b.outSeq;
      });
    var names = [];
    for (var i = 0; i < n && i < outs.length; i++) {
      outs[i].out = false;
      outs[i].outSeq = 0;
      names.push(outs[i].name);
    }
    return names;
  }
  function reviveAll(k, L) {
    k.squads[L].players.forEach(function (p) {
      p.out = false;
      p.outSeq = 0;
    });
  }
  function rotateRaid(k) {
    k.raidingTeam = otherL(k.raidingTeam);
    k.raidTimeLeft = k.raidTimeLimit || 0; // fresh raid clock for every new raid
    k.raidTimeUp = false;
    k.raidTimerPaused = false;
    var onMat = onMatPlayers(k, k.raidingTeam);
    if (!onMat.length) {
      setRaider(k, null);
      return;
    }
    var idx = k.raiderIdx[k.raidingTeam] || 0;
    setRaider(k, onMat[idx % onMat.length]);
    k.raiderIdx[k.raidingTeam] = idx + 1;
  }
  // keep the raider valid (must be a player of the raiding team who is on the mat)
  function ensureRaider(k) {
    var p =
      k.raiderId != null ? kbFindPlayer(k, k.raidingTeam, k.raiderId) : null;
    if (p && p.on && !p.out) return;
    setRaider(k, onMatPlayers(k, k.raidingTeam)[0] || null);
  }
  function endRaid(k) {
    k.raidNumber++;
    k.doOrDie = false;
    rotateRaid(k);
  }
  function autoAllOut(k, loserL, winnerL) {
    if (activeCount(k, loserL) > 0) return;
    if (
      !k.squads[loserL].players.some(function (p) {
        return p.on;
      })
    )
      return;
    kbTeamObj(k, winnerL).score += 2;
    reviveAll(k, loserL);
    logEvent("All out", kbTeamObj(k, loserL).name + " all out (+2)", true);
  }

  /* ---- pickers ---- */
  function playerOptions(list) {
    return list.map(function (p) {
      return { label: p.name + (p.role ? " · " + p.role : ""), value: p.id };
    });
  }
  function touchPicker(isSuper) {
    var k = state.kabaddi,
      defL = otherL(k.raidingTeam),
      opts = playerOptions(onMatPlayers(k, defL));
    return {
      kind: "kb-touch",
      multi: true,
      isSuper: !!isSuper,
      title: isSuper ? "Super raid (3+ touch points)" : "Raid point",
      body:
        k.raider +
        " is raiding. Select every defender the raider touched — they all go out." +
        (isSuper ? " Select at least 3." : ""),
      showBonus: true,
      bonus: false,
      selected: [],
      options: opts,
    };
  }
  function tacklePicker() {
    var k = state.kabaddi,
      defL = otherL(k.raidingTeam);
    return {
      kind: "kb-tackle",
      multi: true,
      title: "Tackle point",
      body:
        "Who tackled " +
        k.raider +
        "? Select the defender(s) — the raider goes out.",
      showBonus: false,
      selected: [],
      options: playerOptions(onMatPlayers(k, defL)),
    };
  }
  function raiderPicker() {
    var k = state.kabaddi;
    return {
      kind: "kb-raider",
      title: "Select raider",
      body: kbTeamObj(k, k.raidingTeam).name + " — choose who raids next.",
      options: onMatPlayers(k, k.raidingTeam).map(function (p) {
        return {
          label: p.name + (p.id === k.raiderId ? " (current)" : ""),
          value: p.id,
        };
      }),
    };
  }
  function technicalPicker() {
    var k = state.kabaddi;
    return {
      kind: "kb-tech",
      title: "Technical point",
      body: "Which team gets the technical point?",
      options: [
        { label: k.teamA.name, value: "A" },
        { label: k.teamB.name, value: "B" },
      ],
    };
  }
  function setKabaddiRaider(id) {
    var k = state.kabaddi,
      p = kbFindPlayer(k, k.raidingTeam, id);
    if (!p || !p.on || p.out) return;
    pushHistory("kabaddi");
    setRaider(state.kabaddi, p);
    logEvent(
      "Raider selected",
      p.name + " — " + kbTeamObj(k, k.raidingTeam).name
    );
    render();
  }

  function kabaddiDispatch(action) {
    if (action.type === "UNDO_CONFIRM") {
      handleUndoConfirm();
      return;
    }
    if (action.type === "ALL_OUT_CONFIRM") {
      state.pendingConfirm = {
        title: "Confirm all out",
        body: "Mark the defending team all out? This awards 2 points to the raiding team and brings back all defending players.",
        confirmLabel: "Confirm all out",
        danger: false,
        kind: "kabaddi-allout",
      };
      render();
      return;
    }
    var k = state.kabaddi,
      t = action.type,
      defL = otherL(k.raidingTeam);

    if (
      t === "RAID_POINT" ||
      t === "SUPER_RAID" ||
      t === "TACKLE_POINT" ||
      t === "CHANGE_RAIDER"
    ) {
      ensureRaider(k);
      if (!k.raiderId) {
        notice(
          "No raider on the mat",
          "The raiding team has nobody on the mat. Bring a player to the mat from SQUADS or use Switch raid team."
        );
        return;
      }
      if (t === "CHANGE_RAIDER") {
        state.picker = raiderPicker();
        render();
        return;
      }
      if (!activeCount(k, defL)) {
        notice(
          "No defenders on the mat",
          "The defending team has nobody on the mat. Use All out or fix the squads first."
        );
        return;
      }
      state.picker =
        t === "TACKLE_POINT" ? tacklePicker() : touchPicker(t === "SUPER_RAID");
      render();
      return;
    }
    if (t === "TECHNICAL_POINT") {
      state.picker = technicalPicker();
      render();
      return;
    }
    applyKabaddi(action);
  }

  function applyKabaddi(action) {
    pushHistory("kabaddi");
    var next = clone(state.kabaddi),
      raidL = next.raidingTeam,
      defL = otherL(raidL),
      raidTeam = kbTeamObj(next, raidL),
      defTeam = kbTeamObj(next, defL),
      t = action.type;

    if (t === "RAID_POINT") {
      var bonus = !!action.bonus,
        outNames = setOutIds(next, defL, action.touched || []),
        n = outNames.length + (bonus ? 1 : 0);
      if (n < 1) {
        state.history.pop();
        return;
      }
      raidTeam.score += n;
      var back = reviveN(next, raidL, n);
      logEvent(
        outNames.length >= 3 ? "Super raid" : "Raid point",
        raidTeam.name +
          " — " +
          next.raider +
          " (" +
          plural(n, "point") +
          (outNames.length ? "; out: " + outNames.join(", ") : "") +
          (bonus ? "; bonus" : "") +
          (back.length ? "; back: " + back.join(", ") : "") +
          ")",
        true
      );
      autoAllOut(next, defL, raidL);
      endRaid(next);
    } else if (t === "TACKLE_POINT") {
      var raiderP = kbFindPlayer(next, raidL, next.raiderId);
      if (!raiderP) {
        state.history.pop();
        return;
      }
      var tacklers = (action.tacklers || [])
        .map(function (id) {
          var d = kbFindPlayer(next, defL, id);
          return d ? d.name : "";
        })
        .filter(Boolean);
      setOutIds(next, raidL, [raiderP.id]);
      defTeam.score++;
      var back2 = reviveN(next, defL, 1);
      logEvent(
        "Tackle point",
        defTeam.name +
          " — " +
          (tacklers.join(", ") || "defence") +
          " tackled " +
          raiderP.name +
          (back2.length ? " (back: " + back2.join(", ") + ")" : ""),
        true
      );
      autoAllOut(next, raidL, defL);
      endRaid(next);
    } else if (t === "EMPTY_RAID") {
      if (next.doOrDie) {
        var dp = kbFindPlayer(next, raidL, next.raiderId);
        if (dp) setOutIds(next, raidL, [dp.id]);
        defTeam.score++;
        var back3 = reviveN(next, defL, 1);
        logEvent(
          "Do-or-die failed",
          next.raider +
            " is out, " +
            defTeam.name +
            " +1" +
            (back3.length ? " (back: " + back3.join(", ") + ")" : ""),
          true
        );
        autoAllOut(next, raidL, defL);
      } else {
        logEvent("Empty raid", raidTeam.name + " — " + next.raider);
      }
      endRaid(next);
    } else if (t === "BONUS") {
      raidTeam.score++;
      var back4 = reviveN(next, raidL, 1);
      logEvent(
        "Bonus point",
        raidTeam.name +
          " — " +
          next.raider +
          (back4.length ? " (back: " + back4.join(", ") + ")" : "")
      );
      endRaid(next);
    } else if (t === "ALL_OUT") {
      raidTeam.score += 2;
      reviveAll(next, defL);
      logEvent("All out", defTeam.name + " all out (+2)", true);
    } else if (t === "TECHNICAL_POINT") {
      var tl = action.team === "B" ? "B" : "A";
      kbTeamObj(next, tl).score++;
      logEvent("Technical point", kbTeamObj(next, tl).name);
    } else if (t === "DO_OR_DIE") {
      next.doOrDie = !next.doOrDie;
      logEvent(
        "Do-or-die",
        next.doOrDie ? "Next raid marked do-or-die" : "Do-or-die cleared"
      );
    } else if (t === "TIMEOUT") {
      var tkL =
          action.team === "A" || action.team === "B" ? action.team : raidL,
        tk = kbTeamObj(next, tkL);
      if (tk.timeouts > 0) {
        tk.timeouts--;
        logEvent("Timeout", tk.name);
      } else {
        state.history.pop();
        return;
      }
    } else if (t === "SWITCH_RAID_TEAM") {
      rotateRaid(next);
      logEvent(
        "Raid team switched",
        kbTeamObj(next, next.raidingTeam).name + " — " + next.raider
      );
    } else if (t === "TOGGLE_CLOCK") {
      if (next.timeRemainingSeconds > 0) next.running = !next.running;
      logEvent(
        next.running ? "Clock resumed" : "Clock paused",
        clockLabel(next.timeRemainingSeconds)
      );
    } else if (t === "NEXT_HALF") {
      next.running = false;
      if (next.half === 1) {
        next.half = 2;
        next.timeRemainingSeconds = next.halfLengthSeconds;
        logEvent(
          "Half time",
          next.teamA.name +
            " " +
            next.teamA.score +
            " - " +
            next.teamB.score +
            " " +
            next.teamB.name,
          true
        );
      } else {
        var kres =
          next.teamA.score === next.teamB.score
            ? "Match tied"
            : (next.teamA.score > next.teamB.score
                ? next.teamA.name
                : next.teamB.name) +
              " won by " +
              plural(Math.abs(next.teamA.score - next.teamB.score), "point");
        next.result = kres;
        state.status = "completed";
        logEvent("Full time", kres, true);
      }
    } else {
      state.history.pop();
      return;
    }
    state.kabaddi = next;
    render();
  }

  /* =========================================================
     FOOTBALL
     ========================================================= */
  var SIDE_L = { home: "A", away: "B" };
  function fbPlayers(f, side) {
    return f.squads[SIDE_L[side === "away" ? "away" : "home"]].players;
  }
  function fbOnField(f, side) {
    return fbPlayers(f, side).filter(function (p) {
      return p.on && !p.sentOff;
    });
  }
  function fbBench(f, side) {
    return fbPlayers(f, side).filter(function (p) {
      return !p.on && !p.sentOff;
    });
  }
  function fbFind(f, side, id) {
    return (
      fbPlayers(f, side).filter(function (p) {
        return p.id === id;
      })[0] || null
    );
  }
  function fbName(f, side, id) {
    var p = id != null ? fbFind(f, side, id) : null;
    return p ? p.name : "";
  }
  function fbOptions(list) {
    return list.map(function (p) {
      return {
        label: "#" + p.no + "  " + p.name + " (" + p.pos + ")",
        value: p.id,
      };
    });
  }
  function fbTeamInfo(f, side) {
    return side === "away" ? f.away : f.home;
  }

  function scorerPicker(side) {
    var f = state.football;
    return {
      kind: "fb-scorer",
      side: side,
      title: "Goal — " + fbTeamInfo(f, side).name,
      body: "Who scored the goal?",
      options: fbOptions(fbOnField(f, side)).concat([
        { label: "Unknown / skip", value: null },
      ]),
    };
  }
  function assistPicker(side, scorerId) {
    var f = state.football;
    return {
      kind: "fb-assist",
      side: side,
      scorerId: scorerId,
      title: "Assist (optional) — " + fbName(f, side, scorerId) + " scored",
      body: "Who gave the assist?",
      options: fbOptions(
        fbOnField(f, side).filter(function (p) {
          return p.id !== scorerId;
        })
      ).concat([{ label: "No assist", value: null }]),
    };
  }
  function ownGoalPicker(side) {
    var f = state.football,
      other = side === "home" ? "away" : "home";
    return {
      kind: "fb-own",
      side: side,
      title: "Own goal — " + fbTeamInfo(f, side).name,
      body:
        "Which " +
        fbTeamInfo(f, side).short +
        " player scored it? The goal counts for " +
        fbTeamInfo(f, other).name +
        ".",
      options: fbOptions(fbOnField(f, side)),
    };
  }
  function cardPicker(side, card) {
    var f = state.football;
    return {
      kind: "fb-card",
      side: side,
      card: card,
      title: card + " — " + fbTeamInfo(f, side).name,
      body: "Who received the " + card.toLowerCase() + "?",
      options: fbOptions(fbOnField(f, side)),
    };
  }
  function subOffPicker(side) {
    var f = state.football;
    return {
      kind: "fb-suboff",
      side: side,
      title: "Substitution — " + fbTeamInfo(f, side).name,
      body: "Which player is going OFF?",
      options: fbOptions(fbOnField(f, side)),
    };
  }
  function subOnPicker(side, offId) {
    var f = state.football;
    return {
      kind: "fb-subon",
      side: side,
      offId: offId,
      title: "Substitution — " + fbTeamInfo(f, side).name,
      body: fbName(f, side, offId) + " goes off. Which substitute comes ON?",
      options: fbOptions(fbBench(f, side)),
    };
  }

  function footballDispatch(action) {
    if (action.type === "UNDO_CONFIRM") {
      handleUndoConfirm();
      return;
    }
    var f = state.football,
      t = action.type,
      side;

    // buttons open a player picker first; the picker (or the squads popup) re-enters with playerId set
    if (t === "GOAL" && action.playerId === undefined) {
      side = action.side === "away" ? "away" : "home";
      if (!fbOnField(f, side).length) {
        notice("No players on the field", "Add players from SQUADS first.");
        return;
      }
      state.picker = scorerPicker(side);
      render();
      return;
    }
    if (t === "OWN_GOAL" && action.playerId === undefined) {
      side = f.selectedSide;
      if (!fbOnField(f, side).length) {
        notice("No players on the field", "Add players from SQUADS first.");
        return;
      }
      state.picker = ownGoalPicker(side);
      render();
      return;
    }
    if (t === "CARD" && action.playerId === undefined) {
      side = action.side || f.selectedSide;
      if (!fbOnField(f, side).length) {
        notice("No players on the field", "Add players from SQUADS first.");
        return;
      }
      state.picker = cardPicker(side, action.card);
      render();
      return;
    }
    if (t === "SUBSTITUTION" && action.offId === undefined) {
      side = f.selectedSide;
      if (!fbBench(f, side).length) {
        notice(
          "No substitutes available",
          "There is nobody left on the bench for " +
            fbTeamInfo(f, side).name +
            "."
        );
        return;
      }
      state.picker = subOffPicker(side);
      render();
      return;
    }
    applyFootball(action);
  }

  function applyFootball(action) {
    pushHistory("football");
    var next = clone(state.football),
      cl = clockLabel(next.seconds),
      t = action.type;
    if (t === "GOAL") {
      var key = action.side === "away" ? "away" : "home",
        scorer = fbName(next, key, action.playerId),
        assist = fbName(next, key, action.assistId);
      next[key].score++;
      next.events.unshift({
        time: cl,
        type: "Goal",
        team:
          next[key].short +
          (scorer ? " · " + scorer : "") +
          (assist ? " (assist " + assist + ")" : ""),
      });
      logEvent(
        "Goal",
        (scorer ? scorer + " — " : "") +
          next[key].name +
          (assist ? " (assist: " + assist + ")" : ""),
        true
      );
    } else if (t === "OWN_GOAL") {
      var ogSide = action.side || next.selectedSide,
        scoringSide = ogSide === "home" ? "away" : "home",
        ogName = fbName(next, ogSide, action.playerId);
      next[scoringSide].score++;
      next.events.unshift({
        time: cl,
        type: "Own goal",
        team: next[ogSide].short + (ogName ? " · " + ogName : ""),
      });
      logEvent(
        "Own goal",
        (ogName ? ogName + " — " : "") +
          next[ogSide].name +
          " (counts for " +
          next[scoringSide].name +
          ")",
        true
      );
    } else if (t === "CARD") {
      var cardSide = action.side || next.selectedSide,
        short = next[cardSide].short,
        cardPlayer =
          action.playerId != null
            ? fbFind(next, cardSide, action.playerId)
            : null,
        cname = cardPlayer ? cardPlayer.name : "",
        sendOff = action.card === "Red card";
      next.events.unshift({
        time: cl,
        type: action.card,
        team: short + (cname ? " · " + cname : ""),
      });
      logEvent(action.card, (cname ? cname + " — " : "") + short);
      if (action.card === "Yellow card" && cardPlayer) {
        var ck = cardSide + ":" + cardPlayer.id;
        next.cards[ck] = (next.cards[ck] || 0) + 1;
        if (next.cards[ck] >= 2) {
          sendOff = true;
          next.events.unshift({
            time: cl,
            type: "Red card",
            team: short + " · " + cname + " (2nd yellow)",
          });
          logEvent("Red card", cname + " — " + short + " (2nd yellow)", true);
        }
      }
      if (sendOff && cardPlayer) {
        cardPlayer.on = false;
        cardPlayer.sentOff = true;
      }
    } else if (t === "SUBSTITUTION") {
      var subSide = action.side || next.selectedSide,
        off = fbFind(next, subSide, action.offId),
        on = fbFind(next, subSide, action.onId);
      if (!off || !on || !off.on || on.on || on.sentOff) {
        state.history.pop();
        return;
      }
      off.on = false;
      on.on = true;
      next.events.unshift({
        time: cl,
        type: "Substitution",
        team: next[subSide].short + " · " + off.name + " → " + on.name,
      });
      logEvent(
        "Substitution",
        next[subSide].name + ": " + off.name + " off, " + on.name + " on"
      );
    } else if (t === "EVENT") {
      var short2 =
        next.selectedSide === "home" ? next.home.short : next.away.short;
      next.events.unshift({ time: cl, type: action.event, team: short2 });
      logEvent(action.event, short2);
    } else if (t === "SELECT_SIDE") {
      next.selectedSide = action.side === "away" ? "away" : "home";
    } else if (t === "TOGGLE_CLOCK") {
      next.running = !next.running;
      logEvent(next.running ? "Clock resumed" : "Clock paused", cl);
    } else if (t === "ADD_TIME") {
      next.addedTime++;
      logEvent("Stoppage time", "+" + next.addedTime + " min announced");
    } else if (t === "NEXT_HALF") {
      next.running = false;
      if (next.half === 1) {
        next.half = 2;
        next.seconds = 45 * 60;
        next.addedTime = 0;
        logEvent(
          "Half time",
          next.home.name +
            " " +
            next.home.score +
            " - " +
            next.away.score +
            " " +
            next.away.name,
          true
        );
      } else {
        var fres =
          next.home.score === next.away.score
            ? "Match drawn"
            : (next.home.score > next.away.score
                ? next.home.name
                : next.away.name) +
              " won " +
              Math.max(next.home.score, next.away.score) +
              "-" +
              Math.min(next.home.score, next.away.score);
        next.result = fres;
        state.status = "completed";
        logEvent("Full time", fres, true);
      }
    } else {
      state.history.pop();
      return;
    }
    if (next.events.length > 40) next.events.length = 40;
    state.football = next;
    render();
  }

  /* =========================================================
     UNDO
     ========================================================= */
  function handleUndoConfirm() {
    if (!state.history.length) {
      notice("Nothing to undo", "There are no recorded actions to undo.");
      return;
    }
    var last = state.eventLog[0];
    state.pendingConfirm = {
      title: "Undo last action",
      body: last
        ? "Undo: " + last.label + " — " + last.detail
        : "Undo the last recorded action?",
      confirmLabel: "Undo",
      danger: true,
      kind: "undo",
    };
    render();
  }
  function doUndo() {
    var snap = state.history.pop();
    if (!snap) return;
    state[snap.sport] = snap.state;
    state.status = snap.status;
    state.eventLog = snap.eventLog;
    state.queuedCount = snap.queuedCount;
  }

  /* =========================================================
     CCSquads popup
     ========================================================= */
  window.CCSquads = (function () {
    var overlayEl = null;
    function open(opts) {
      close();
      var sport = opts.sport || "cricket",
        dataFn = opts.getData,
        onAction = opts.onAction,
        currentTeam = "A",
        subIn = null,
        selPid = null,
        msg = "";
      overlayEl = document.createElement("div");
      overlayEl.className = "cc-sq-overlay";

      function renderPlayerDot(p) {
        return (
          '<div class="cc-sq-fbp' +
          (selPid === p.id ? " sel" : "") +
          '" data-pid="' +
          p.id +
          '"><div class="cc-sq-fbdot">' +
          p.no +
          '</div><span class="cc-sq-fbn">' +
          escapeHtml(p.name) +
          "</span></div>"
        );
      }

      function renderModal() {
        if (!overlayEl) return;
        var data = dataFn(),
          teams = data.teams,
          tObj = teams[currentTeam],
          tc = tObj.color || "#ff6a13",
          contentHtml = "";

        if (sport === "cricket") {
          contentHtml =
            '<div class="cc-sq-list">' +
            tObj.players
              .map(function (p, idx) {
                var stClass =
                  p.status === "bat"
                    ? "bat"
                    : p.status === "out"
                    ? "out"
                    : p.status === "next"
                    ? "next"
                    : "";
                var stText =
                  p.status === "bat"
                    ? "BATTING"
                    : p.status === "out"
                    ? "OUT"
                    : p.status === "next"
                    ? "NEXT IN"
                    : "YET TO BAT";
                var tagHtml = p.role
                  ? '<span class="cc-sq-tag ' +
                    escapeHtml(p.role.toLowerCase()) +
                    '">' +
                    escapeHtml(p.role) +
                    "</span>"
                  : "";
                var actionBtn = "";
                if (currentTeam === "A" && p.status === "yet")
                  actionBtn =
                    '<button class="cc-sq-act" data-pid="' +
                    p.id +
                    '" data-action="next">' +
                    (data.awaiting ? "Send In" : "Send Next") +
                    "</button>";
                else if (currentTeam === "A" && p.status === "next")
                  actionBtn =
                    '<button class="cc-sq-act red" data-pid="' +
                    p.id +
                    '" data-action="next">Cancel Next</button>';
                var showStats =
                  currentTeam === "A" &&
                  (p.status === "bat" || p.status === "out");
                var howHtml =
                  showStats && p.how
                    ? '<small class="cc-sq-how">' +
                      escapeHtml(p.how) +
                      "</small>"
                    : "";
                var hasStats = p.runs !== undefined && p.runs !== null;
                var scoreHtml =
                  showStats && hasStats
                    ? '<span class="cc-sq-runs"><b>' +
                      p.runs +
                      "</b><small>(" +
                      (p.balls || 0) +
                      ")</small></span>"
                    : "";
                return (
                  '<div class="cc-sq-row ' +
                  stClass +
                  '"><span class="no">' +
                  (idx + 1) +
                  '</span><span class="nm">' +
                  escapeHtml(p.name) +
                  howHtml +
                  "</span>" +
                  tagHtml +
                  scoreHtml +
                  '<span class="cc-sq-st">' +
                  stText +
                  "</span>" +
                  actionBtn +
                  "</div>"
                );
              })
              .join("") +
            "</div>";
        } else if (sport === "kabaddi") {
          contentHtml =
            '<div class="cc-sq-kbgrid">' +
            tObj.players
              .map(function (p) {
                return (
                  '<div class="cc-sq-kb ' +
                  (p.on ? "on" : "") +
                  (p.on && p.out ? " out" : "") +
                  '" data-pid="' +
                  p.id +
                  '" data-action="' +
                  (p.on ? "to_bench" : "to_mat") +
                  '"><div class="ph">' +
                  p.id +
                  '</div><div class="nm">' +
                  escapeHtml(p.name) +
                  '</div><div class="rl">' +
                  escapeHtml(p.role) +
                  (p.on && p.out ? " · OUT" : "") +
                  "</div></div>"
                );
              })
              .join("") +
            "</div>";
        } else if (sport === "football") {
          var starters = tObj.players.filter(function (p) {
              return p.on;
            }),
            subs = tObj.players.filter(function (p) {
              return !p.on && !p.sentOff;
            }),
            selPlayer = starters.filter(function (p) {
              return p.id === selPid;
            })[0],
            actBar = selPlayer
              ? '<div class="cc-sq-actbar"><b>' +
                escapeHtml(selPlayer.name) +
                '</b><button class="cc-sq-act" data-fa="goal">GOAL</button>' +
                '<button class="cc-sq-act" data-fa="yellow" style="background:#facc15;border-color:#facc15;color:#1a1400;">YELLOW CARD</button>' +
                '<button class="cc-sq-act" data-fa="red" style="background:#dc2626;border-color:#dc2626;color:#fff;">RED CARD</button></div>'
              : "";
          contentHtml =
            '<div class="cc-sq-pitch">' +
            ["FWD", "MID", "DEF", "GK"]
              .map(function (pos) {
                return (
                  '<div class="cc-sq-fbline">' +
                  starters
                    .filter(function (p) {
                      return p.pos === pos;
                    })
                    .map(renderPlayerDot)
                    .join("") +
                  "</div>"
                );
              })
              .join("") +
            "</div>" +
            actBar +
            '<div class="cc-sq-bench">' +
            subs
              .map(function (p) {
                return (
                  '<div class="cc-sq-chip' +
                  (subIn === p.id ? " sel" : "") +
                  '" data-pid="' +
                  p.id +
                  '" data-action="substitute">' +
                  p.no +
                  ". " +
                  escapeHtml(p.name) +
                  " (" +
                  escapeHtml(p.pos) +
                  ")</div>"
                );
              })
              .join("") +
            '</div><div class="cc-sq-footnote">' +
            (subIn !== null
              ? "Now tap the starter who is going off."
              : "Tap a starter for goal / card actions, or tap a bench player first to make a substitution.") +
            "</div>";
        }

        var alertHtml =
          (data.awaiting
            ? '<div class="cc-sq-alert">Batter is out! Please select the next batter from the squad.</div>'
            : "") +
          (msg ? '<div class="cc-sq-alert">' + escapeHtml(msg) + "</div>" : "");

        overlayEl.innerHTML =
          '<div class="cc-sq-modal"><div class="cc-sq-top"><div class="cc-sq-title">SQUADS MANAGEMENT</div><div class="cc-sq-pills"><button class="cc-sq-pill ' +
          (currentTeam === "A" ? "on" : "") +
          '" data-team="A">' +
          escapeHtml(teams.A.name) +
          '</button><button class="cc-sq-pill ' +
          (currentTeam === "B" ? "on" : "") +
          '" data-team="B">' +
          escapeHtml(teams.B.name) +
          '</button></div><button class="cc-sq-close" aria-label="Close">&times;</button></div>' +
          alertHtml +
          '<div class="cc-sq-tv" style="--tc:' +
          escapeHtml(tc) +
          '"><div class="cc-sq-head"><div class="cc-sq-badge">' +
          escapeHtml(tObj.short) +
          '</div><div><div class="cc-sq-name">' +
          escapeHtml(tObj.name) +
          '</div><div class="cc-sq-sub">Squad Lineup &amp; Actions</div></div></div>' +
          contentHtml +
          '<div class="cc-sq-foot"><span>ChampionCall Match Manager</span><span>Click actions to update match state</span></div></div></div>';
      }

      overlayEl.addEventListener("click", function (e) {
        if (e.target === overlayEl) {
          close();
          return;
        }
        if (e.target.closest(".cc-sq-close")) {
          close();
          return;
        }
        msg = "";

        var pill = e.target.closest("[data-team]");
        if (pill) {
          currentTeam = pill.getAttribute("data-team");
          subIn = null;
          selPid = null;
          renderModal();
          return;
        }

        var fa = e.target.closest("[data-fa]");
        if (fa && selPid !== null) {
          onAction({
            sport: sport,
            team: currentTeam,
            playerId: selPid,
            action: fa.getAttribute("data-fa"),
          });
          selPid = null;
          renderModal();
          return;
        }

        var target = e.target.closest("[data-pid]");
        if (!target) return;
        var pid = parseInt(target.getAttribute("data-pid"), 10),
          act = target.getAttribute("data-action");

        if (sport === "football") {
          if (act === "substitute") {
            subIn = subIn === pid ? null : pid;
            selPid = null;
            renderModal();
            return;
          }
          if (subIn !== null) {
            onAction({
              sport: sport,
              team: currentTeam,
              playerId: subIn,
              outId: pid,
              action: "substitute",
            });
            subIn = null;
            renderModal();
            return;
          }
          selPid = selPid === pid ? null : pid;
          renderModal();
          return;
        }

        if (sport === "kabaddi" && act === "to_mat") {
          var onMat = dataFn().teams[currentTeam].players.filter(function (x) {
            return x.on;
          }).length;
          if (onMat >= 7) {
            msg =
              "Only 7 players can be on the mat. Move someone to the bench first.";
            renderModal();
            return;
          }
        }

        onAction({
          sport: sport,
          team: currentTeam,
          playerId: pid,
          action: act || (sport === "kabaddi" ? "to_mat" : "next"),
        });
        renderModal();
      });

      document.body.appendChild(overlayEl);
      renderModal();
    }
    function close() {
      if (overlayEl && overlayEl.parentNode)
        overlayEl.parentNode.removeChild(overlayEl);
      overlayEl = null;
    }
    return { open: open, close: close };
  })();

  function openSquads(sportName) {
    if (!window.CCSquads || !state) return;
    var sp = sportName || state.sport;
    window.CCSquads.open({
      sport: sp,
      getData: function () {
        var s = state[sp];
        return {
          teams: s.squads,
          battingTeam: sp === "cricket" ? "A" : null,
          awaiting: sp === "cricket" && !!s.awaitingBatter,
        };
      },
      onAction: squadsAction,
    });
  }

  function squadsAction(a) {
    if (!state) return;
    var s = state[a.sport],
      p =
        s &&
        s.squads[a.team] &&
        s.squads[a.team].players.filter(function (x) {
          return x.id === a.playerId;
        })[0];
    if (!p) return;

    if (a.sport === "cricket" && a.action === "next") {
      if (p.status === "out" || p.status === "bat") return;
      var wasNext = p.status === "next",
        filling = s.awaitingBatter;
      pushHistory("cricket");
      s.squads.A.players.forEach(function (x) {
        if (x.status === "next") x.status = "yet";
      });
      if (filling) {
        p.status = "bat";
        s[s.awaitingSlot || "striker"] = { name: p.name, runs: 0, balls: 0 };
        s.awaitingBatter = false;
        s.awaitingSlot = null;
        logEvent("New batter", p.name + " comes in", true);
      } else if (wasNext) {
        logEvent("Next batter cancelled", p.name);
      } else {
        p.status = "next";
        logEvent("Next batter", p.name + " is next in");
      }
      render();
      if (filling && s.awaitingBowler && state.status === "live" && !s.result) {
        state.picker = bowlerPicker();
        render();
      }
    } else if (a.sport === "kabaddi") {
      var goOn = a.action === "to_mat";
      if (p.on === goOn) return;
      pushHistory("kabaddi");
      p.on = goOn;
      if (!goOn) {
        p.out = false;
        p.outSeq = 0;
      }
      ensureRaider(s);
      logEvent(
        goOn ? "To mat" : "To bench",
        p.name + " — " + kbTeamObj(s, a.team).name
      );
      render();
    } else if (a.sport === "football") {
      var sideKey = a.team === "A" ? "home" : "away";
      if (a.action === "substitute") {
        if (p.on || p.sentOff || a.outId == null) return;
        footballDispatch({
          type: "SUBSTITUTION",
          side: sideKey,
          offId: a.outId,
          onId: p.id,
        });
      } else if (a.action === "goal") {
        footballDispatch({ type: "GOAL", side: sideKey, playerId: p.id });
      } else if (a.action === "yellow" || a.action === "red") {
        footballDispatch({
          type: "CARD",
          side: sideKey,
          card: a.action === "yellow" ? "Yellow card" : "Red card",
          playerId: p.id,
        });
      }
    }
  }

  /* =========================================================
     RENDERING
     ========================================================= */
  function scoreText(n) {
    return String(n == null ? 0 : n);
  }

  function renderCricket() {
    var c = state.cricket;
    var remaining = Math.max(0, c.oversLimit * 6 - c.legalBalls);
    var need = Math.max(0, c.target - c.teamA.score);
    var runRate = c.legalBalls
      ? (c.teamA.score / (c.legalBalls / 6)).toFixed(2)
      : "0.00";
    var requiredRate = remaining ? (need / (remaining / 6)).toFixed(2) : "—";

    function team(t, overs, cls) {
      return (
        '<div class="cc-cr-team ' +
        cls +
        '"><div class="cc-cr-team-name">' +
        escapeHtml(t.name) +
        '</div><div class="cc-cr-total">' +
        scoreText(t.score) +
        "/" +
        scoreText(t.wickets) +
        '</div><div class="cc-cr-overs">Overs ' +
        escapeHtml(overs) +
        "</div></div>"
      );
    }
    function stat(label, value) {
      return "<div><span>" + label + "</span><b>" + value + "</b></div>";
    }
    function runBtn(v) {
      return (
        '<button data-action="RUN" data-value="' +
        v +
        '"' +
        (v === 4 || v === 6 ? ' class="boundary"' : "") +
        ">" +
        v +
        "</button>"
      );
    }
    function player(p, cls) {
      return (
        '<div class="cc-cr-player' +
        (cls ? " " + cls : "") +
        '"><span>' +
        escapeHtml(p.name) +
        (cls === "striker" ? " <b>*</b>" : "") +
        "</span><strong>" +
        p.runs +
        " <small>(" +
        p.balls +
        ")</small></strong></div>"
      );
    }

    var banners = "";
    if (c.result)
      banners +=
        '<div class="cc-cr-result"><strong>' +
        escapeHtml(c.result) +
        "</strong></div>";
    if (c.freeHit) banners += '<div class="cc-cr-freehit">FREE HIT</div>';
    if (c.awaitingBatter)
      banners +=
        '<div class="cc-cr-alert"><span>Wicket! Select the next batter to continue.</span><button data-action="OPEN_SQUADS">Select batter</button></div>';
    if (c.awaitingBowler)
      banners +=
        '<div class="cc-cr-alert"><span>Over complete. Select the next bowler.</span><button data-action="SELECT_BOWLER">Select bowler</button></div>';

    return (
      '<div class="cc-scoreboard cc-cricket">' +
      '<div class="cc-match-meta"><span>' +
      escapeHtml(c.round) +
      "</span><span>" +
      escapeHtml(state.status.toUpperCase()) +
      "</span></div>" +
      '<div class="cc-sb-grid">' +
      '<div class="cc-sb-info">' +
      '<div class="cc-cr-score">' +
      team(c.teamA, ov(c.legalBalls), "batting") +
      team(c.teamB, c.teamB.overs || "20.0", "") +
      "</div>" +
      '<div class="cc-cr-stats">' +
      stat("Run rate", runRate) +
      stat("Target", c.target) +
      stat("Need", need + " off " + remaining) +
      stat("Req. rate", requiredRate) +
      "</div>" +
      '<div class="cc-cr-batters"><div class="cc-cr-section-title">BATTING</div>' +
      player(c.striker, "striker") +
      player(c.nonStriker, "") +
      "</div>" +
      '<div class="cc-cr-bowler"><div class="cc-cr-section-title">BOWLING</div><div class="cc-cr-player"><span>' +
      escapeHtml(c.bowler.name) +
      "</span><strong>" +
      ov(c.bowler.balls) +
      " <small>ov</small> · " +
      c.bowler.runs +
      " <small>runs</small> · " +
      c.bowler.wickets +
      " <small>wkts</small></strong></div></div>" +
      '<div class="cc-cr-over"><div class="cc-cr-section-title">THIS OVER</div><div class="cc-cr-balls">' +
      c.currentOverBalls
        .map(function (b) {
          return (
            '<span class="cc-cr-ball ' +
            escapeHtml(b.type) +
            '">' +
            escapeHtml(b.label) +
            "</span>"
          );
        })
        .join("") +
      "</div></div>" +
      "</div>" +
      '<div class="cc-sb-ctrl">' +
      banners +
      '<div class="cc-cr-actions">' +
      [0, 1, 2, 3, 4, 6].map(runBtn).join("") +
      '<button data-action="WICKET_CONFIRM" class="danger">WICKET</button>' +
      '<button data-action="WIDE">WIDE</button><button data-action="NO_BALL">NO BALL</button><button data-action="BYE">BYE</button><button data-action="LEG_BYE">LEG BYE</button></div>' +
      '<div class="cc-cr-tools"><button data-action="OPEN_SQUADS">Manage squads</button><button data-action="SWAP_STRIKE">Swap strike</button><button data-action="UNDO_CONFIRM">' +
      icon("undo", 13) +
      " Undo</button></div>" +
      "</div></div></div>"
    );
  }

  function renderKabaddi() {
    var k = state.kabaddi;
    var raidTeam = kbTeamObj(k, k.raidingTeam);

    function teamHtml(team, key) {
      var lineup = k.squads[key].players.filter(function (p) {
        return p.on;
      });
      var outNames = lineup
        .filter(function (p) {
          return p.out;
        })
        .sort(function (a, b) {
          return a.outSeq - b.outSeq;
        })
        .map(function (p) {
          return p.name;
        });
      return (
        '<div class="cc-kb-team' +
        (k.raidingTeam === key ? " raiding" : "") +
        '"><div class="cc-kb-team-name">' +
        escapeHtml(team.name) +
        '</div><div class="cc-kb-score">' +
        team.score +
        '</div><div class="cc-kb-players">' +
        lineup
          .map(function (p) {
            return (
              '<span class="cc-kb-player ' +
              (p.out ? "inactive" : "active") +
              '" title="' +
              escapeHtml(p.name) +
              '">' +
              (p.out ? "○" : "●") +
              "</span>"
            );
          })
          .join("") +
        '</div><div class="cc-kb-timeouts">On mat ' +
        activeCount(k, key) +
        "/" +
        lineup.length +
        " · Timeouts " +
        team.timeouts +
        "</div>" +
        (outNames.length
          ? '<div class="cc-kb-outlist">Out: ' +
            escapeHtml(outNames.join(", ")) +
            "</div>"
          : "") +
        '<button data-action="TIMEOUT" data-team="' +
        key +
        '">Timeout</button></div>'
      );
    }

    var banners = "";
    if (k.result)
      banners +=
        '<div class="cc-kb-result"><strong>' +
        escapeHtml(k.result) +
        "</strong></div>";
    if (k.doOrDie) banners += '<div class="cc-kb-doordie">DO OR DIE RAID</div>';
    if (k.raidTimeLimit > 0 && k.raidTimeUp && !k.result)
      banners +=
        '<div class="cc-kb-raidup"><span>RAID TIME UP — ' +
        escapeHtml(k.raider) +
        " has not scored." +
        (k.doOrDie ? " Do-or-die: raider is out." : "") +
        '</span><button type="button" data-action="EMPTY_RAID">End raid (empty)</button></div>';

    return (
      '<div class="cc-scoreboard cc-kabaddi">' +
      '<div class="cc-match-meta"><span>' +
      escapeHtml(k.round) +
      "</span><span>HALF " +
      k.half +
      "</span></div>" +
      '<div class="cc-sb-grid">' +
      '<div class="cc-sb-info">' +
      '<div class="cc-kb-scoreboard">' +
      teamHtml(k.teamA, "A") +
      '<div class="cc-kb-center"><div class="cc-kb-clock">' +
      clockLabel(k.timeRemainingSeconds) +
      '</div><div class="cc-kb-status">' +
      (k.running ? "LIVE" : "PAUSED") +
      '</div><div class="cc-kb-raid">Raid #' +
      k.raidNumber +
      "</div>" +
      (k.raidTimeLimit > 0
        ? '<div class="cc-kb-raidtimer' +
          (k.raidTimeUp
            ? " up"
            : k.raidTimerPaused
            ? " paused"
            : k.raidTimeLeft <= 5
            ? " low"
            : "") +
          '"><span class="cc-kb-raidtimer-label">' +
          (k.raidTimerPaused ? "RAID STOPPED" : "RAID") +
          '</span> <b id="ccRaidLeft">' +
          k.raidTimeLeft +
          "</b>s</div>"
        : "") +
      "</div>" +
      teamHtml(k.teamB, "B") +
      "</div>" +
      '<div class="cc-kb-raid-team">Raiding: <strong>' +
      escapeHtml(raidTeam.name) +
      '</strong> &nbsp;·&nbsp; Raider: <button type="button" class="cc-kb-raider-btn" data-action="CHANGE_RAIDER" title="Change raider">' +
      escapeHtml(k.raider) +
      " ▾</button></div>" +
      '<div class="cc-kb-timerset"><label for="ccRaidSecs">Raid timer (sec)</label>' +
      '<input id="ccRaidSecs" type="number" min="0" max="120" step="1" value="' +
      k.raidTimeLimit +
      '"' +
      (k.raidTimerLocked ? " disabled" : "") +
      ">" +
      (k.raidTimerLocked
        ? '<button type="button" id="ccRaidUnlock" title="Raid timer is locked">🔒 Change</button>'
        : '<button type="button" id="ccRaidSet">Set &amp; lock</button>') +
      '<button type="button" id="ccRaidToggle"' +
      (k.raidTimeLimit > 0 ? "" : " disabled") +
      ">" +
      (k.raidTimerPaused ? "▶ Start timer" : "⏸ Stop timer") +
      "</button>" +
      '<button type="button" id="ccRaidReset" title="Restart the raid timer"' +
      (k.raidTimeLimit > 0 ? "" : " disabled") +
      ">↻ Reset</button>" +
      '<span class="cc-kb-timerhint">' +
      (k.raidTimerLocked
        ? "Locked for this match"
        : "0 = no raid timer · locks after Set") +
      "</span></div>" +
      "</div>" +
      '<div class="cc-sb-ctrl">' +
      banners +
      '<div class="cc-kb-actions"><button data-action="RAID_POINT">Raid point</button><button data-action="TACKLE_POINT">Tackle point</button><button data-action="EMPTY_RAID">Empty raid</button><button data-action="BONUS">Bonus</button><button data-action="SUPER_RAID">Super raid (+3)</button><button data-action="TECHNICAL_POINT">Technical point</button><button data-action="DO_OR_DIE">' +
      (k.doOrDie ? "Do or die: ON" : "Do or die") +
      '</button><button data-action="SWITCH_RAID_TEAM">Switch raid team</button><button data-action="TOGGLE_CLOCK">' +
      (k.running ? "Pause clock" : "Resume clock") +
      '</button><button data-action="NEXT_HALF">' +
      (k.half === 1 ? "End half" : "End match") +
      '</button><button data-action="ALL_OUT_CONFIRM" class="danger">All out</button></div>' +
      '<div class="cc-kb-tools"><button data-action="OPEN_SQUADS">Manage squads</button><button data-action="UNDO_CONFIRM">' +
      icon("undo", 13) +
      " Undo</button></div>" +
      "</div></div></div>"
    );
  }

  function renderFootball() {
    var f = state.football;

    function sideBtn(side, team) {
      return (
        '<button data-action="SELECT_SIDE" data-side="' +
        side +
        '"' +
        (f.selectedSide === side ? ' class="active"' : "") +
        ">" +
        escapeHtml(team.short) +
        "</button>"
      );
    }
    function evClass(t) {
      t = String(t).toLowerCase();
      if (t.indexOf("yellow") > -1) return "yc";
      if (t.indexOf("red") > -1) return "rc";
      if (t.indexOf("goal") > -1) return "goal";
      return "";
    }

    var banners = "";
    if (f.result)
      banners +=
        '<div class="cc-fb-result"><strong>' +
        escapeHtml(f.result) +
        "</strong></div>";

    return (
      '<div class="cc-scoreboard cc-football">' +
      '<div class="cc-match-meta"><span>' +
      escapeHtml(f.round) +
      "</span><span>HALF " +
      f.half +
      (f.addedTime ? " +" + f.addedTime : "") +
      "</span></div>" +
      '<div class="cc-sb-grid">' +
      '<div class="cc-sb-info">' +
      '<div class="cc-fb-score"><div class="cc-fb-team"><div class="cc-fb-team-name">' +
      escapeHtml(f.home.name) +
      '</div><div class="cc-fb-total">' +
      f.home.score +
      '</div></div><div class="cc-fb-vs">VS</div><div class="cc-fb-team"><div class="cc-fb-team-name">' +
      escapeHtml(f.away.name) +
      '</div><div class="cc-fb-total">' +
      f.away.score +
      "</div></div></div>" +
      /* ===== CHANGED: clock + START/PAUSE button ===== */
      '<div class="cc-clock-row">' +
      '<div class="cc-fb-clock">' +
      clockLabel(f.seconds) +
      "</div>" +
      '<button type="button" class="cc-clock-btn' +
      (f.running ? "" : " paused") +
      '" data-action="TOGGLE_CLOCK">' +
      (f.running ? icon("pause", 16) + " PAUSE" : icon("play", 16) + " START") +
      "</button>" +
      "</div>" +
      /* ===== END CHANGE ===== */

      '<div class="cc-fb-events"><div class="cc-section-title">MATCH EVENTS</div>' +
      (f.events
        .map(function (e) {
          return (
            '<div class="cc-fb-event ' +
            evClass(e.type) +
            '"><span>' +
            escapeHtml(e.time) +
            "</span><strong>" +
            escapeHtml(e.type) +
            "</strong><span>" +
            escapeHtml(e.team) +
            "</span></div>"
          );
        })
        .join("") || '<div class="cc-eventlog-empty">No events yet</div>') +
      "</div>" +
      "</div>" +
      '<div class="cc-sb-ctrl">' +
      banners +
      '<div class="cc-fb-side">Cards, own goal, substitution &amp; corners apply to: ' +
      sideBtn("home", f.home) +
      sideBtn("away", f.away) +
      "</div>" +
      '<div class="cc-fb-actions"><button data-action="GOAL" data-side="home">Home goal</button><button data-action="GOAL" data-side="away">Away goal</button><button data-action="OWN_GOAL">Own goal</button><button data-action="CARD" data-card="Yellow card">Yellow card</button><button data-action="CARD" data-card="Red card">Red card</button><button data-action="SUBSTITUTION">Substitution</button><button data-action="EVENT" data-event="Corner kick">Corner kick</button><button data-action="ADD_TIME">+1 min stoppage</button><button data-action="TOGGLE_CLOCK">' +
      (f.running ? "Pause clock" : "Resume clock") +
      '</button><button data-action="NEXT_HALF">' +
      (f.half === 1 ? "End half" : "End match") +
      "</button></div>" +
      '<div class="cc-fb-tools"><button data-action="OPEN_SQUADS">Manage squads</button><button data-action="UNDO_CONFIRM">' +
      icon("undo", 13) +
      " Undo</button></div>" +
      "</div></div></div>"
    );
  }
  function renderEventLog() {
    return (
      '<div class="cc-card"><div class="cc-section-title">LIVE EVENT LOG</div><div class="cc-eventlog-list">' +
      (state.eventLog
        .map(function (e) {
          return (
            '<div class="cc-eventlog-row"><span class="cc-eventlog-time">' +
            escapeHtml(e.time) +
            "</span><strong>" +
            escapeHtml(e.label) +
            "</strong><span>" +
            escapeHtml(e.detail) +
            "</span></div>"
          );
        })
        .join("") || '<div class="cc-eventlog-empty">No events yet</div>') +
      "</div></div>"
    );
  }

  function renderModal() {
    var p = state.pendingConfirm;
    if (!p) return "";
    return (
      '<div class="cc-modal-backdrop"><div class="cc-modal"><div class="cc-modal-title">' +
      icon("alert", 16) +
      escapeHtml(p.title) +
      '</div><div class="cc-modal-body">' +
      escapeHtml(p.body) +
      '</div><div class="cc-modal-actions"><button type="button" id="ccModalCancel">Cancel</button><button type="button" id="ccModalConfirm" class="' +
      (p.danger ? "danger" : "") +
      '">' +
      escapeHtml(p.confirmLabel || "Confirm") +
      "</button></div></div></div>"
    );
  }

  function renderPicker() {
    var p = state.picker;
    if (!p) return "";
    var selStyle = "background:#ff6a13;border-color:#ff6a13;color:#111;";
    var opts = p.options
      .map(function (o, i) {
        var on = p.multi && p.selected.indexOf(o.value) !== -1;
        return (
          '<button type="button" data-pick="' +
          i +
          '"' +
          (on ? ' class="cc-pick-sel" style="' + selStyle + '"' : "") +
          ">" +
          (on ? "✓ " : "") +
          escapeHtml(o.label) +
          "</button>"
        );
      })
      .join("");
    var extra = "";
    if (p.multi) {
      extra +=
        '<div class="cc-picker-meta">Selected: ' + p.selected.length + "</div>";
      if (p.showBonus)
        extra +=
          '<div style="margin:10px 0;"><button type="button" id="ccPickerBonus" class="cc-pick-bonus' +
          (p.bonus ? " on" : "") +
          '" aria-pressed="' +
          (p.bonus ? "true" : "false") +
          '"><span class="cc-pick-bonus-box">' +
          (p.bonus ? "✓" : "") +
          '</span><span class="cc-pick-bonus-txt">Bonus point scored too <b>+1</b></span></button></div>';
    }
    if (p.error)
      extra += '<div class="cc-picker-err">' + escapeHtml(p.error) + "</div>";
    return (
      '<div class="cc-modal-backdrop"><div class="cc-modal"><div class="cc-modal-title">' +
      escapeHtml(p.title) +
      "</div>" +
      (p.body
        ? '<div class="cc-modal-body">' + escapeHtml(p.body) + "</div>"
        : "") +
      '<div class="cc-picker-grid" style="display:flex;flex-wrap:wrap;gap:8px;margin:12px 0;">' +
      opts +
      "</div>" +
      extra +
      '<div class="cc-modal-actions"><button type="button" id="ccPickerCancel">Cancel</button>' +
      (p.multi
        ? '<button type="button" id="ccPickerConfirm">Confirm</button>'
        : "") +
      "</div></div></div>"
    );
  }

  function renderHeader() {
    return (
      '<div class="cc-header"><div class="cc-header-title"><strong>CHAMPIONCALL</strong><span>SCOREKEEPER</span></div><div class="cc-header-meta">' +
      "<span>" +
      escapeHtml(state.matchName || "Match #" + (state.matchId || "—")) +
      "</span><span>Scorekeeper: " +
      escapeHtml(state.scorekeeper || "—") +
      "</span>" +
      '<span class="' +
      (state.online ? "online" : "offline") +
      '">' +
      (state.online ? "CONNECTED" : "CONNECTION LOST") +
      "</span>" +
      '<select id="ccStatusSelect">' +
      ["upcoming", "live", "paused", "halftime", "innings-break", "completed"]
        .map(function (s) {
          return (
            '<option value="' +
            s +
            '"' +
            (state.status === s ? " selected" : "") +
            ">" +
            s.toUpperCase() +
            "</option>"
          );
        })
        .join("") +
      "</select>" +
      '<button data-squads-open="1">SQUADS</button><button data-lock-action="1">' +
      icon("lock", 13) +
      " EXIT / LOCK</button></div></div>"
    );
  }

  function renderStatusStrip() {
    return (
      '<div class="cc-statusstrip"><span class="cc-statusbadge">' +
      (state.status === "live"
        ? "● LIVE"
        : escapeHtml(state.status.toUpperCase())) +
      "</span>" +
      (state.queuedCount > 0
        ? '<span class="cc-queue-note">Syncing ' +
          state.queuedCount +
          " queued events</span>"
        : "") +
      "</div>"
    );
  }

  function renderTabs() {
    return (
      '<div class="cc-tabs">' +
      SPORTS.map(function (s) {
        return (
          '<button class="cc-tab' +
          (state.sport === s ? " active" : "") +
          '" data-sport-tab="' +
          s +
          '">' +
          s.toUpperCase() +
          "</button>"
        );
      }).join("") +
      "</div>"
    );
  }

  function render() {
    if (!root || !state) return;
    var locked = state.status !== "live";
    var cardHtml = "";
    if (state.sport === "cricket") cardHtml = renderCricket();
    else if (state.sport === "kabaddi") cardHtml = renderKabaddi();
    else if (state.sport === "football") cardHtml = renderFootball();

    root.innerHTML =
      '<div class="cc-wrap">' +
      renderHeader() +
      renderStatusStrip() +
      renderTabs() +
      '<div class="cc-matchcard-wrap">' +
      cardHtml +
      renderEventLog() +
      "</div>" +
      renderModal() +
      renderPicker() +
      "</div>";

    if (locked) {
      root
        .querySelectorAll(".cc-matchcard-wrap [data-action]")
        .forEach(function (b) {
          var a = b.getAttribute("data-action");
          if (a !== "OPEN_SQUADS" && a !== "UNDO_CONFIRM") b.disabled = true;
        });
    }
    bindEvents();
  }

  /* ---------------- Events ---------------- */
  function bindEvents() {
    var squadsBtn = root.querySelector("[data-squads-open]");
    if (squadsBtn)
      squadsBtn.addEventListener("click", function () {
        openSquads();
      });

    var lockBtn = root.querySelector("[data-lock-action]");
    if (lockBtn)
      lockBtn.addEventListener("click", function () {
        state.pendingConfirm = {
          title: "Exit / lock scorekeeper",
          body: "Pause scoring? Scoring controls will be disabled until the match status is changed.",
          confirmLabel: "Pause match",
          danger: true,
          kind: "lock",
        };
        render();
      });

    var cancel = root.querySelector("#ccModalCancel");
    if (cancel) cancel.addEventListener("click", modalCancel);

    var confirm = root.querySelector("#ccModalConfirm");
    if (confirm) confirm.addEventListener("click", modalConfirm);

    var pickCancel = root.querySelector("#ccPickerCancel");
    if (pickCancel)
      pickCancel.addEventListener("click", function () {
        state.picker = null;
        render();
      });

    root.querySelectorAll("[data-pick]").forEach(function (btn) {
      btn.addEventListener("click", function () {
        pickerChoose(parseInt(btn.getAttribute("data-pick"), 10));
      });
    });

    var pickConfirm = root.querySelector("#ccPickerConfirm");
    if (pickConfirm) pickConfirm.addEventListener("click", pickerConfirm);

    var raidSet = root.querySelector("#ccRaidSet");
    if (raidSet)
      raidSet.addEventListener("click", function () {
        var inp = root.querySelector("#ccRaidSecs"),
          v = inp ? parseInt(inp.value, 10) : NaN;
        if (isNaN(v)) v = 0;
        v = Math.max(0, Math.min(120, v));
        var k = state.kabaddi;
        k.raidTimeLimit = v;
        k.raidTimeLeft = v;
        k.raidTimeUp = false;
        k.raidTimerPaused = false;
        k.raidTimerLocked = v > 0; // once a timer is set it is locked
        logEvent(
          "Raid timer",
          v
            ? "Set to " + v + " seconds per raid (locked)"
            : "Raid timer turned off"
        );
        render();
      });

    var raidUnlock = root.querySelector("#ccRaidUnlock");
    if (raidUnlock)
      raidUnlock.addEventListener("click", function () {
        state.pendingConfirm = {
          title: "Change raid timer?",
          body: "The raid timer is locked for this match. Unlock it to set a new value?",
          confirmLabel: "Unlock",
          danger: false,
          kind: "raid-timer-unlock",
        };
        render();
      });

    var raidToggle = root.querySelector("#ccRaidToggle");
    if (raidToggle)
      raidToggle.addEventListener("click", function () {
        var k = state.kabaddi;
        if (!(k.raidTimeLimit > 0)) return;
        k.raidTimerPaused = !k.raidTimerPaused;
        logEvent(
          "Raid timer",
          k.raidTimerPaused
            ? "Stopped at " + k.raidTimeLeft + "s"
            : "Resumed at " + k.raidTimeLeft + "s"
        );
        render();
      });

    var raidReset = root.querySelector("#ccRaidReset");
    if (raidReset)
      raidReset.addEventListener("click", function () {
        var k = state.kabaddi;
        k.raidTimeLeft = k.raidTimeLimit || 0;
        k.raidTimeUp = false;
        render();
      });

    var pickBonus = root.querySelector("#ccPickerBonus");
    if (pickBonus)
      pickBonus.addEventListener("click", function () {
        if (!state.picker) return;
        state.picker.bonus = !state.picker.bonus;
        state.picker.error = "";
        render();
      });

    var statusSelect = root.querySelector("#ccStatusSelect");
    if (statusSelect)
      statusSelect.addEventListener("change", function (e) {
        state.status = e.target.value;
        render();
      });

    root.querySelectorAll("[data-sport-tab]").forEach(function (btn) {
      btn.addEventListener("click", function () {
        var s = btn.getAttribute("data-sport-tab");
        if (SPORTS.indexOf(s) !== -1) {
          state.sport = s;
          render();
        }
      });
    });

    root.querySelectorAll("[data-action]").forEach(function (btn) {
      btn.addEventListener("click", function () {
        if (btn.disabled || !state) return;

        var actionType = btn.getAttribute("data-action");
        var value = btn.getAttribute("data-value");
        var side = btn.getAttribute("data-side");
        var card = btn.getAttribute("data-card");
        var event = btn.getAttribute("data-event");
        var team = btn.getAttribute("data-team");

        if (actionType === "OPEN_SQUADS") {
          openSquads();
          return;
        }

        var action = { type: actionType };
        if (value !== null) action.value = parseInt(value, 10);
        if (side !== null) action.side = side;
        if (card !== null) action.card = card;
        if (event !== null) action.event = event;
        if (team !== null) action.team = team;

        if (state.sport === "cricket") cricketDispatch(action);
        else if (state.sport === "kabaddi") kabaddiDispatch(action);
        else if (state.sport === "football") footballDispatch(action);
      });
    });
  }

  function pickerChoose(i) {
    var p = state.picker;
    if (!p) return;
    var o = p.options[i];
    if (!o) return;

    if (p.multi) {
      var at = p.selected.indexOf(o.value);
      if (at === -1) p.selected.push(o.value);
      else p.selected.splice(at, 1);
      p.error = "";
      render();
      return;
    }

    state.picker = null;
    if (p.kind === "dismissal") {
      if (state.cricket.freeHit && o.value !== "Run out") {
        notice(
          "Free hit",
          "On a free hit the batter can only be dismissed run out. Record the ball as a dot or runs instead."
        );
        return;
      }
      if (["Caught", "Stumped", "Run out"].indexOf(o.value) !== -1) {
        state.picker = fielderPicker(o.value);
        render();
      } else applyCricket({ type: "WICKET", how: o.value });
    } else if (p.kind === "fielder") {
      applyCricket({ type: "WICKET", how: p.how, fielder: o.value });
    } else if (p.kind === "extra") {
      applyCricket({ type: p.extraType, value: o.value });
    } else if (p.kind === "bowler") {
      setBowler(o.value);
    } else if (p.kind === "kb-raider") {
      setKabaddiRaider(o.value);
    } else if (p.kind === "kb-tech") {
      applyKabaddi({ type: "TECHNICAL_POINT", team: o.value });
    } else if (p.kind === "fb-scorer") {
      if (o.value === null)
        applyFootball({ type: "GOAL", side: p.side, playerId: null });
      else {
        state.picker = assistPicker(p.side, o.value);
        render();
      }
    } else if (p.kind === "fb-assist") {
      applyFootball({
        type: "GOAL",
        side: p.side,
        playerId: p.scorerId,
        assistId: o.value,
      });
    } else if (p.kind === "fb-own") {
      applyFootball({ type: "OWN_GOAL", side: p.side, playerId: o.value });
    } else if (p.kind === "fb-card") {
      applyFootball({
        type: "CARD",
        side: p.side,
        card: p.card,
        playerId: o.value,
      });
    } else if (p.kind === "fb-suboff") {
      state.picker = subOnPicker(p.side, o.value);
      render();
    } else if (p.kind === "fb-subon") {
      applyFootball({
        type: "SUBSTITUTION",
        side: p.side,
        offId: p.offId,
        onId: o.value,
      });
    } else render();
  }

  function pickerConfirm() {
    var p = state.picker;
    if (!p || !p.multi) return;
    var sel = p.selected.slice();
    if (p.kind === "kb-touch") {
      if (p.isSuper && sel.length < 3) {
        p.error = "A super raid needs at least 3 touched defenders.";
        render();
        return;
      }
      if (!sel.length && !p.bonus) {
        p.error = "Select the touched defenders, or tick the bonus point.";
        render();
        return;
      }
      state.picker = null;
      applyKabaddi({
        type: "RAID_POINT",
        touched: sel,
        bonus: p.bonus,
        superRaid: p.isSuper,
      });
    } else if (p.kind === "kb-tackle") {
      if (!sel.length) {
        p.error = "Select at least one defender who made the tackle.";
        render();
        return;
      }
      state.picker = null;
      applyKabaddi({ type: "TACKLE_POINT", tacklers: sel });
    }
  }

  function modalConfirm() {
    var pc = state.pendingConfirm;
    if (!pc) return;
    state.pendingConfirm = null;

    if (pc.kind === "cricket-wicket") {
      state.picker = dismissalPicker();
      render();
      return;
    }
    if (pc.kind === "raid-timer-unlock") {
      state.kabaddi.raidTimerLocked = false;
      render();
      return;
    }
    if (pc.kind === "kabaddi-allout") {
      applyKabaddi({ type: "ALL_OUT" });
      return;
    }
    if (pc.kind === "undo") doUndo();
    else if (pc.kind === "lock") state.status = "paused";
    render();
  }

  function modalCancel() {
    state.pendingConfirm = null;
    render();
  }

  function onOnline() {
    if (!state) return;
    state.online = true;
    render();

    if (state.queuedCount > 0) {
      clearTimeout(queuedResetTimer);
      queuedResetTimer = setTimeout(function () {
        if (!state) return;
        state.queuedCount = 0;
        render();
      }, 600);
    }
  }

  function onOffline() {
    if (!state) return;
    state.online = false;
    render();
  }

  /* Clocks update only their own text so open dropdowns / dialogs aren't wiped every second */
  function tick() {
    if (!root || !document.body.contains(root)) {
      window.destroyScorekeeper();
      return;
    }
    if (!state || state.status !== "live") return;

    if (state.sport === "football" && state.football.running) {
      state.football.seconds++;
      var fe = root.querySelector(".cc-fb-clock");
      if (fe) fe.textContent = clockLabel(state.football.seconds);
    } else if (
      state.sport === "kabaddi" &&
      state.kabaddi.running &&
      state.kabaddi.timeRemainingSeconds > 0
    ) {
      state.kabaddi.timeRemainingSeconds--;

      // raid clock: counts down for the current raid, flags "time up" at zero
      var kb = state.kabaddi;
      if (
        kb.raidTimeLimit > 0 &&
        !kb.raidTimeUp &&
        !kb.raidTimerPaused &&
        !kb.result
      ) {
        if (kb.raidTimeLeft > 0) kb.raidTimeLeft--;
        var rl = root.querySelector("#ccRaidLeft");
        if (rl) {
          rl.textContent = kb.raidTimeLeft;
          var rt = rl.parentNode;
          if (rt)
            rt.className =
              "cc-kb-raidtimer" + (kb.raidTimeLeft <= 5 ? " low" : "");
        }
        if (kb.raidTimeLeft === 0) {
          kb.raidTimeUp = true;
          logEvent(
            "Raid time up",
            kb.raider + " — raid clock reached zero",
            true
          );
          render();
          return;
        }
      }

      if (state.kabaddi.timeRemainingSeconds === 0) {
        state.kabaddi.running = false;
        logEvent("Half time", "Kabaddi clock reached zero", true);
        render();
      } else {
        var ke = root.querySelector(".cc-kb-clock");
        if (ke) ke.textContent = clockLabel(state.kabaddi.timeRemainingSeconds);
      }
    }
  }

  /* ---------------- Extra styles for the new controls ---------------- */
  var EXTRA_CSS =
    ".cc-clock-row{display:flex;align-items:center;justify-content:center;gap:14px;margin:6px 0}" +
    ".cc-clock-btn{display:inline-flex;align-items:center;gap:6px;padding:8px 18px;border-radius:999px;border:1px solid #ef4444;background:rgba(239,68,68,.12);color:#fca5a5;font:inherit;font-weight:800;letter-spacing:.05em;cursor:pointer}" +
    ".cc-clock-btn.paused{border-color:#4ade80;background:rgba(74,222,128,.14);color:#4ade80}" +
    ".cc-clock-btn:disabled{opacity:.45;cursor:not-allowed}" +
    ".cc-icon{fill:none;stroke:currentColor;stroke-width:2}" +
    ".cc-kb-raider-btn{background:transparent;border:1px dashed #ff9a4d;color:#ff9a4d;border-radius:6px;padding:2px 10px;font:inherit;font-weight:700;cursor:pointer;margin-left:2px}" +
    ".cc-kb-raider-btn:hover:not(:disabled){background:rgba(255,154,77,.14)}" +
    ".cc-kb-raider-btn{background:transparent;border:1px dashed #ff9a4d;color:#ff9a4d;border-radius:6px;padding:2px 10px;font:inherit;font-weight:700;cursor:pointer;margin-left:2px}" +
    ".cc-kb-raider-btn:hover:not(:disabled){background:rgba(255,154,77,.14)}" +
    ".cc-kb-raider-btn:disabled{opacity:.5;cursor:not-allowed}" +
    ".cc-kb-outlist{margin-top:6px;font-size:11px;line-height:1.3;color:#f87171;opacity:.9}" +
    ".cc-picker-meta{color:#9ca3af;font-size:12px;margin:2px 0}" +
    ".cc-picker-err{color:#f87171;font-size:13px;margin:6px 0}" +
    ".cc-sq-kb.out{opacity:.55;outline:1px dashed #f87171}" +
    /* bonus toggle in the raid-point popup */
    ".cc-pick-bonus{display:inline-flex;align-items:center;gap:10px;padding:10px 16px;border-radius:10px;border:1px solid #3a3f4b;background:#171a21;color:#e5e7eb;font:inherit;font-weight:600;font-size:14px;cursor:pointer;transition:background .15s,border-color .15s,color .15s}" +
    ".cc-pick-bonus:hover{border-color:#facc15}" +
    ".cc-pick-bonus-box{display:inline-flex;align-items:center;justify-content:center;width:20px;height:20px;border-radius:6px;border:2px solid #6b7280;font-size:14px;font-weight:800;line-height:1;color:#1a1400}" +
    ".cc-pick-bonus.on{background:rgba(250,204,21,.14);border-color:#facc15;color:#facc15}" +
    ".cc-pick-bonus.on .cc-pick-bonus-box{background:#facc15;border-color:#facc15}" +
    ".cc-pick-bonus b{color:#facc15}" +
    /* raid timer */
    ".cc-kb-raidtimer{margin-top:8px;display:inline-flex;align-items:baseline;gap:6px;padding:4px 14px;border-radius:999px;border:1px solid #3a3f4b;background:#171a21;color:#e5e7eb;font-size:13px;font-weight:700;letter-spacing:.04em}" +
    ".cc-kb-raidtimer b{font-size:20px;font-variant-numeric:tabular-nums;color:#4ade80}" +
    ".cc-kb-raidtimer-label{font-size:11px;color:#9ca3af}" +
    ".cc-kb-raidtimer.low{border-color:#f59e0b}.cc-kb-raidtimer.low b{color:#f59e0b}" +
    ".cc-kb-raidtimer.up{border-color:#ef4444;background:rgba(239,68,68,.12)}.cc-kb-raidtimer.up b{color:#ef4444}" +
    ".cc-kb-timerset{display:flex;flex-wrap:wrap;align-items:center;gap:8px;margin-top:10px;padding:8px 12px;border:1px solid #2a2f3a;border-radius:10px;font-size:13px;color:#9ca3af}" +
    ".cc-kb-timerset input{width:70px;padding:6px 8px;border-radius:8px;border:1px solid #3a3f4b;background:#0f1218;color:#fff;font:inherit;font-weight:700;text-align:center}" +
    ".cc-kb-timerset button{padding:6px 14px;border-radius:8px;border:1px solid #3a3f4b;background:#171a21;color:#e5e7eb;font:inherit;font-weight:700;cursor:pointer}" +
    ".cc-kb-timerset button:hover:not(:disabled){border-color:#ff9a4d;color:#ff9a4d}" +
    ".cc-kb-timerhint{font-size:11px;opacity:.7}" +
    ".cc-kb-timerset input:disabled{opacity:.6;cursor:not-allowed;border-style:dashed}" +
    ".cc-kb-timerset button:disabled{opacity:.45;cursor:not-allowed}" +
    ".cc-kb-raidtimer.paused{border-color:#60a5fa;background:rgba(96,165,250,.1)}.cc-kb-raidtimer.paused b{color:#60a5fa}" +
    ".cc-kb-raidup{display:flex;flex-wrap:wrap;align-items:center;justify-content:space-between;gap:10px;margin-bottom:10px;padding:10px 14px;border-radius:10px;border:1px solid #ef4444;background:rgba(239,68,68,.12);color:#fca5a5;font-weight:700;font-size:13px}" +
    ".cc-kb-raidup button{padding:6px 12px;border-radius:8px;border:1px solid #ef4444;background:#ef4444;color:#fff;font:inherit;font-weight:700;cursor:pointer}";
  function injectStyles() {
    if (document.getElementById("cc-scorekeeper-extra")) return;
    var el = document.createElement("style");
    el.id = "cc-scorekeeper-extra";
    el.textContent = EXTRA_CSS;
    document.head.appendChild(el);
  }

  /* ---------------- Public API ---------------- */
  window.initScorekeeper = function () {
    if (typeof window.destroyScorekeeper === "function") {
      window.destroyScorekeeper();
    }

    var host = document.getElementById("scorekeeperRoot");
    if (!host) return;

    const matchId = host.dataset.matchId;

    // Retrieve the scorer's token saved after successful OTP verification.
    const storedMatchId = sessionStorage.getItem("scorerMatchId");

    // Only use the token if it belongs to this particular match.
    const scorerToken =
      storedMatchId === String(matchId)
        ? sessionStorage.getItem("scorerToken")
        : null;

    // Scorer sends the token; organizer connects without a scorer token.
    window.CricketWS.connect(
      matchId,
      function (updatedState) {
        console.log("New score state:", updatedState);
      },
      {
        scorerSessionToken: scorerToken,
      }
    );

    root = host;
    state = createState();
    injectStyles();

    window.addEventListener("online", onOnline);
    window.addEventListener("offline", onOffline);

    tickTimer = setInterval(tick, 1000);
    render();
  };

  window.destroyScorekeeper = function () {
    if (window.CricketWS && window.CricketWS.disconnect) {
      window.CricketWS.disconnect();
    }

    if (tickTimer) {
      clearInterval(tickTimer);
      tickTimer = null;
    }
    if (queuedResetTimer) {
      clearTimeout(queuedResetTimer);
      queuedResetTimer = null;
    }

    window.removeEventListener("online", onOnline);
    window.removeEventListener("offline", onOffline);

    if (window.CCSquads) window.CCSquads.close();
    if (root) root.innerHTML = "";
    var extra = document.getElementById("cc-scorekeeper-extra");
    if (extra && extra.parentNode) extra.parentNode.removeChild(extra);

    root = null;
    state = null;
  };
})();
