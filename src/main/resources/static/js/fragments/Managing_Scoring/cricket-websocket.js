const CricketWS = (function () {
  let stompClient = null;
  let matchId = null;
  let onStateUpdate = null;
  let onError = null;
  let onStatusChange = null;
  let scorerSessionToken = null;
  let reconnectTimer = null;
  let reconnectAttempts = 0;
  let manuallyClosed = false;

  // NEW: remembers eventIds of balls sent, so undo knows which ball to target
  const ballEventIds = [];
  let lastSent = null; // { type, eventId } of the last message sent

  // NEW: crypto.randomUUID() only exists on https or localhost
  function newEventId() {
    if (window.crypto && crypto.randomUUID) return crypto.randomUUID();
    return "evt-" + Date.now() + "-" + Math.random().toString(16).slice(2);
  }

  // ---------------------------------------------------------
  // CONNECT
  // ---------------------------------------------------------
  function connect(id, callback, options) {
    options = options || {};
    matchId = id;
    onStateUpdate = callback;

    // Store the scorer's session token, if this connection belongs to a scorer.
    scorerSessionToken = options.scorerSessionToken || null;

    onError =
      options.onError ||
      function (msg) {
        console.error(msg);
      };
    onStatusChange = options.onStatusChange || function () {};
    manuallyClosed = false;

    openSocket();
  }

  function openSocket() {
    clearTimeout(reconnectTimer);

    const socket = new SockJS("/ws");
    stompClient = Stomp.over(socket);
    stompClient.debug = null;

    const connectHeaders = {};

    if (scorerSessionToken) {
        connectHeaders.scorerSessionToken = scorerSessionToken;
    }


    // 1) Scorer verifies OTP.
    // 2) Backend returns sessionToken.
    // 3) Frontend passes it to CricketWS.connect().
    // 4) JavaScript sends it in STOMP CONNECT headers.
    // 5) WebSocketAuthInterceptor reads scorerSessionToken.
    // 6) It validates the token and authorizes the scorer.

    // send the scorer token for verification
    stompClient.connect(connectHeaders, onConnected, onDisconnected);
  }

  function onConnected() {
    console.log("WebSocket connected");
    reconnectAttempts = 0;
    onStatusChange("connected");

    // live score updates (same topic as your backend)
    stompClient.subscribe(
      "/topic/matches/" + matchId + "/cricket",
      function (message) {
        const state = JSON.parse(message.body);
        if (onStateUpdate) onStateUpdate(state);
      }
    );

    // NEW: errors from the server (e.g. "Select striker, non-striker and bowler")
    stompClient.subscribe("/user/queue/errors", function (message) {
      handleServerError(message.body);
    });

    // NEW: fetch the current score. Needed on first load and after a reconnect,
    // because messages sent while we were offline are lost.
    loadInitialState();
  }

  function onDisconnected(error) {
    console.error("WebSocket disconnected:", error);
    onStatusChange("disconnected");
    if (!manuallyClosed) scheduleReconnect();
  }

  // NEW: auto-reconnect, waiting 1s, 2s, 4s ... up to 15s between tries
  function scheduleReconnect() {
    const delay = Math.min(1000 * Math.pow(2, reconnectAttempts), 15000);
    reconnectAttempts++;
    onStatusChange("reconnecting");
    reconnectTimer = setTimeout(openSocket, delay);
  }

  // NEW: load the current score by REST
  function loadInitialState() {
    fetch("/api/public/matches/" + matchId + "/cricket/state")
      .then(function (res) {
        if (!res.ok)
          throw new Error("Could not load match state (" + res.status + ")");
        return res.json();
      })
      .then(function (state) {
        if (onStateUpdate) onStateUpdate(state);
      })
      .catch(function (err) {
        console.warn(err.message);
      });
  }

  // ---------------------------------------------------------
  // SEND
  // ---------------------------------------------------------
  function send(payload) {
    if (!stompClient || !stompClient.connected) {
      onError("Not connected to the server. Reconnecting..."); // replaces alert()
      return false;
    }
    stompClient.send(
      "/app/matches/" + matchId + "/cricket/events",
      {},
      JSON.stringify(payload)
    );
    lastSent = { type: payload.actionType, eventId: payload.eventId };
    return true;
  }

  // NEW: one place for the default fields, so you don't repeat them 3 times
  function buildPayload(actionType, fields) {
    return Object.assign(
      {
        matchId: Number(matchId),
        eventId: newEventId(),
        actionType: actionType,
        runsOffBat: 0,
        completedRuns: 0,
        extraType: "NONE",
        extraRuns: 0,
        wicket: false,
        wicketType: null,
        dismissedBatterId: null,
        newBatterId: null,
        fielderId: null,
        targetEventId: null,
      },
      fields || {}
    );
  }

  function sendBall(fields) {
    const payload = buildPayload("BALL", fields);
    if (send(payload)) ballEventIds.push(payload.eventId);
  }

  // targetEventId is optional: if you don't pass it, the last ball sent is undone
  function sendUndo(targetEventId) {
    const target = targetEventId || ballEventIds[ballEventIds.length - 1];
    if (!target) {
      onError("Nothing to undo");
      return;
    }
    if (send(buildPayload("UNDO", { targetEventId: target }))) {
      lastSent.undoTarget = target;
    }
  }

  function sendSwapStrike() {
    send(buildPayload("SWAP_STRIKE"));
  }

  // NEW: if the server rejected our last message, fix our local undo stack
  function handleServerError(message) {
    if (lastSent && lastSent.type === "BALL") {
      const i = ballEventIds.lastIndexOf(lastSent.eventId);
      if (i !== -1) ballEventIds.splice(i, 1); // that ball was never saved
    }
    lastSent = null;
    onError(message);
  }

  // ---------------------------------------------------------
  // DISCONNECT
  // ---------------------------------------------------------
  function disconnect() {
    manuallyClosed = true;
    clearTimeout(reconnectTimer);
    if (stompClient && stompClient.connected) stompClient.disconnect();
    stompClient = null;
  }

  return {
    connect: connect,
    disconnect: disconnect,
    sendBall: sendBall,
    sendUndo: sendUndo,
    sendSwapStrike: sendSwapStrike,
    isConnected: function () {
      return !!(stompClient && stompClient.connected);
    },
  };
})();

window.CricketWS = CricketWS;
