
package com.champions_Call.Controller;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import com.champions_Call.Form.Managing_Score.CricketMatchStateDTO;
import com.champions_Call.Form.Managing_Score.CricketScoringRequest;
import com.champions_Call.service.CricketScoringService;

@Controller
public class CricketWebSocketController {

    private final CricketScoringService cricketScoringService;
    private final SimpMessagingTemplate messagingTemplate;

    public CricketWebSocketController(
            CricketScoringService cricketScoringService,
            SimpMessagingTemplate messagingTemplate) {

        this.cricketScoringService = cricketScoringService;
        this.messagingTemplate = messagingTemplate;
    }

    // Receives scoring events from the frontend.
    // WebSocketAuthInterceptor checks whether the sender is authorized
    // before this method is executed.
    @MessageMapping("/matches/{matchId}/cricket/events")
    public void handleScoringEvents(
            @DestinationVariable Long matchId,
            CricketScoringRequest request) {

        // Ensure the request contains the same match ID as the destination.
        if (request == null || !matchId.equals(request.matchId())) {
            throw new IllegalArgumentException("Match Id mismatched...");
        }

        // Process the scoring event and get the updated scoreboard.
        CricketMatchStateDTO updatedScore =
                cricketScoringService.processScoringEvent(request);

        // Broadcast the updated scoreboard to all subscribed viewers.
        messagingTemplate.convertAndSend(
                "/topic/matches/" + matchId + "/cricket",
                updatedScore);
    }

    // Allows viewers to fetch the current scoreboard.
    @GetMapping("/api/public/matches/{matchId}/cricket/state")
    @ResponseBody
    public CricketMatchStateDTO getState(@PathVariable Long matchId) {
        return cricketScoringService.getMatchState(matchId);
    }

    // Handles exceptions thrown inside controller message handling.
    @MessageExceptionHandler
    @SendToUser("/queue/errors")
    public String handleError(Exception ex) {
        return ex.getMessage();
    }
}
