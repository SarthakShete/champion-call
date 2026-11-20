package com.champions_Call.Configuration;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.messaging.Message;

import com.champions_Call.Helper.AccessStatus;
import com.champions_Call.model.match.Match;
import com.champions_Call.model.match.ScoringAccess;
import com.champions_Call.repository.MatchRepository;
import com.champions_Call.repository.ScoringAccessRepository;
import com.champions_Call.service.ScorerVerificationService;
import com.twilio.twiml.voice.Prompt.For;

import org.springframework.transaction.annotation.Transactional;

// WebSocketAuthInterceptor -> so Spring checks incoming STOMP messages before they reach your controller

// Why use ChannelInterceptor?
// Ans : 
// Spring provides ChannelInterceptor so we can inspect incoming messages before they reach the application.

// Its preSend() method runs before the message is sent to the next channel handler.
@Component 
public class WebSocketAuthInterceptor implements ChannelInterceptor{

    // Its responsibilities will be:

    // 1. When a client connects through STOMP, identify whether it is an organizer or scorer.
    // 2. For an organizer, use the existing Spring Security authentication.
    // 3. For a scorer, read the session token from the STOMP CONNECT headers and call validateSession().
    // 4. Store the authenticated identity in the WebSocket session.
    // 5. Before accepting a scoring message, verify that the sender has exclusive scoring permission for that match.


    private final MatchRepository matchRepository;
    private final ScoringAccessRepository scoringAccessRepository;
    private final ScorerVerificationService scorerVerificationService;

    // Constructor injection:
    // We need MatchRepository to identify the match organizer.
    // We need ScoringAccessRepository to identify the latest assignment.
    // We need ScorerVerificationService to validate scorer sessions.
    public WebSocketAuthInterceptor(
            MatchRepository matchRepository,
            ScoringAccessRepository scoringAccessRepository,
            ScorerVerificationService scorerVerificationService) {

        this.matchRepository = matchRepository;
        this.scoringAccessRepository = scoringAccessRepository;
        this.scorerVerificationService = scorerVerificationService;
    }

    /*
     * preSend() executes before an incoming STOMP message
     * reaches its destination.
     *
     * This is our central security checkpoint.
     */
    @Override
    @Transactional(readOnly = true)
    public Message<?> preSend(
            Message<?> message,
            MessageChannel channel) {

        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(
                        message, StompHeaderAccessor.class);

        if (accessor == null) {
            throw new IllegalArgumentException(
                    "Invalid WebSocket message");
        }

        StompCommand command = accessor.getCommand();

        if (command == null) {
            return message;
        }

        /*
         * STEP 1: CONNECT
         *
         * CONNECT happens when a client establishes its STOMP session.
         *
         * A scorer sends the session token received after successful OTP
         * verification in the CONNECT native header.
         *
         * We validate that token immediately and store it in the
         * WebSocket session so we can validate it again on every SEND.
         *
         * Viewers and organizers do not need a scorer session token.
         */
        if (StompCommand.CONNECT.equals(command)) {

            String sessionToken =
                    accessor.getFirstNativeHeader("scorerSessionToken");

            if (sessionToken != null && !sessionToken.isBlank()) {

                // Find which match this scorer session belongs to.
                ScoringAccess access = scoringAccessRepository
                        .findBySessionToken(sessionToken)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Invalid scorer session"));

                Long matchId = access.getMatch().getId();

                // Validate status, OTP verification, latest assignment,
                // match ownership and expiry.
                scorerVerificationService.validateSession(
                        sessionToken, matchId);

                Map<String, Object> sessionAttributes =
                        accessor.getSessionAttributes();

                if (sessionAttributes == null) {
                    throw new IllegalArgumentException(
                            "WebSocket session unavailable");
                }

                // Store the token on the server-side WebSocket session.
                // The client cannot change this stored value through
                // a later SEND message.
                sessionAttributes.put(
                        "scorerSessionToken", sessionToken);

                sessionAttributes.put(
                        "scorerMatchId", matchId);

                // Mark this STOMP session as a scorer session.
                // This is a server-created Principal, not a client claim.
                accessor.setUser(
                        () -> "SCORER:" + access.getId());
            }

            // Allow CONNECT after the checks above.
            // Anonymous viewers can also connect to read scoreboards.
            return message;
        }

        /*
         * STEP 2: SEND
         *
         * SEND means a client is sending a message to our application.
         *
         * We only permit clients to SEND to the cricket scoring endpoint.
         * This prevents clients from sending arbitrary messages to
         * application or broker destinations.
         */
        if (StompCommand.SEND.equals(command)) {
            String destination = accessor.getDestination();

            if (destination == null ||
                    !destination.matches(
                            "/app/matches/\\d+/cricket/events")) {

                throw new IllegalArgumentException(
                        "Unauthorized WebSocket destination");
            }

            // Destination format:
            // /app/matches/{matchId}/cricket/events
            String[] parts = destination.split("/");
            Long matchId = Long.parseLong(parts[3]);

            // Confirm that the match actually exists.
            Match match = matchRepository.findById(matchId)
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Match not found"));

            /*
             * STEP 3: CHECK SCORER SESSION
             *
             * If this WebSocket session belongs to a scorer,
             * validate the saved token again.
             *
             * Why again?
             * Because the organizer might revoke the scorer or
             * assign another scorer while the old scorer is connected.
             */
            Map<String, Object> sessionAttributes =
                    accessor.getSessionAttributes();

            String sessionToken = null;

            if (sessionAttributes != null) {

                Object storedToken =
                        sessionAttributes.get("scorerSessionToken");

                if (storedToken instanceof String) {
                    sessionToken = (String) storedToken;
                }
            }

            if (sessionToken != null) {

                Object storedMatchId =
                        sessionAttributes.get("scorerMatchId");

                if (!(storedMatchId instanceof Long) ||
                        !matchId.equals(storedMatchId)) {

                    throw new IllegalArgumentException(
                            "Scorer is not authorized for this match");
                }

                // This checks whether the scorer is still the latest
                // verified and active assignment for this match.
                scorerVerificationService.validateSession(
                        sessionToken, matchId);

                return message;
            }

            /*
             * STEP 4: ORGANIZER AUTHORIZATION
             *
             * If there is no scorer session token, we check
             * the authenticated Spring Security user.
             */
            Principal principal = accessor.getUser();

            if (principal == null) {
                throw new IllegalArgumentException(
                        "Authentication required");
            }

            String loggedInEmail = principal.getName();

            // Identify the actual organizer who owns this tournament.
            String organizerEmail = match.getTournament()
                    .getOrganizer()
                    .getUser()
                    .getEmail();

            if (!loggedInEmail.equalsIgnoreCase(organizerEmail)) {
                throw new IllegalArgumentException(
                        "You are not authorized to manage this scoreboard");
            }

            /*
             * STEP 5: CHECK LATEST SCORER ASSIGNMENT
             *
             * Even if the logged-in user is the organizer,
             * they cannot score when another scorer has been assigned.
             *
             * PENDING  -> organizer blocked.
             * ACTIVE   -> organizer blocked until expiry.
             * REVOKED  -> organizer can score again.
             * EXPIRED  -> organizer can score again.
             * NO RECORD -> organizer can score.
             */
            ScoringAccess latestAccess =
                    scoringAccessRepository
                            .findTopByMatch_IdOrderByCreatedAtDescIdDesc(
                                    matchId)
                            .orElse(null);

            if (!isOrganizerAllowedToScore(latestAccess)) {

                throw new IllegalArgumentException(
                        "Scoring access has been assigned to another scorer");
            }

            return message;
        }

        /*
         * STEP 6: SUBSCRIBE
         *
         * Viewers need to subscribe to the scoreboard topic
         * to receive real-time updates.
         *
         * But subscribing does not give them permission to score.
         *
         * We only allow subscriptions to the public cricket scoreboard
         * and the user's own error queue.
         */
        if (StompCommand.SUBSCRIBE.equals(command)) {

            String destination = accessor.getDestination();

            if (destination == null ||
                    (!destination.matches(
                            "/topic/matches/\\d+/cricket")
                    && !destination.equals("/user/queue/errors"))) {

                throw new IllegalArgumentException(
                        "Unauthorized subscription destination");
            }

            return message;
        }

        // Other STOMP commands, such as DISCONNECT and UNSUBSCRIBE,
        // are allowed to proceed.
        return message;
    }

    /*
     * Determines whether the organizer can score based on
     * the latest scorer assignment for this match.
     *
     * This method is deliberately separate so the assignment
     * rules are easy to understand and maintain.
     */
    private boolean isOrganizerAllowedToScore(
            ScoringAccess latestAccess) {

        // No scorer has ever been assigned.
        if (latestAccess == null) {
            return true;
        }

        // A revoked assignment allows the organizer to score again.
        if (latestAccess.getStatus() == AccessStatus.REVOKED) {
            return true;
        }

        // A pending assignment blocks both organizer and scorer.
        if (latestAccess.getStatus() == AccessStatus.PENDING) {
            return false;
        }

        // An active assignment blocks the organizer.
        if (latestAccess.getStatus() == AccessStatus.ACTIVE) {

            // Null expiry means access lasts until the match ends.
            if (latestAccess.getExpiresAt() == null) {
                return false;
            }

            // Once the scorer's access expires, organizer regains access.
            return !latestAccess.getExpiresAt()
                    .isAfter(LocalDateTime.now());
        }

        // Fail safely if an unexpected status is encountered.
        return false;
    }
}
