package com.champions_Call.Controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.champions_Call.service.MatchService;
import com.champions_Call.service.EmailNotificationService;
import com.champions_Call.model.match.Match;
import com.champions_Call.Form.Match.*;
import com.champions_Call.model.match.ScoringAccess;
import com.champions_Call.service.ScoringAccessService;




@Controller
@RequestMapping("/api/match")
public class matchController {


  @Autowired
  private MatchService matchService;

  @Autowired
  private ScoringAccessService scoringAccessService;

 

  @PostMapping("/update")
  @ResponseBody
  public ResponseEntity<?> updateMatchSchedule(@RequestBody Map<String, Object> payload) {
      try {
          Object rawMatchId = payload.get("matchId");
          Long matchId = rawMatchId instanceof Number ? ((Number) rawMatchId).longValue() : Long.parseLong(String.valueOf(rawMatchId));

          String datetime = (String) payload.get("datetime"); // "2026-08-20T18:00"
          String venue = (String) payload.get("venue");       // "Stadium A"

          Match match = matchService.getById(matchId);

          System.out.println("The match is found : "+match.getTeamA());

          if(match==null){
             return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Collections.singletonMap("error","Map not found with ID : "+matchId));
          }

          if (datetime != null && !datetime.isEmpty() && datetime.contains("T")) {
                try {
                    String[] parts = datetime.split("T");
                    match.setMatchDate(LocalDate.parse(parts[0])); // e.g. "2026-08-20"
                    
                    String timePart = parts[1].length() == 5 ? parts[1] + ":00" : parts[1];
                    match.setMatchTime(LocalTime.parse(timePart)); // e.g. "18:00:00"
                } catch (Exception e) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(Collections.singletonMap("error", "Invalid date/time format provided."));
                }
            }

            // 4. Update venue if provided
            if (venue != null && !venue.trim().isEmpty()) {
                match.setVenueLocation(venue);
            }

            // 5. Save the updated match back to the database
            matchService.save(match);

            return ResponseEntity.ok(Collections.singletonMap("success", true));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}/details")
    public ResponseEntity<Map<String,Object>> getMatchDetails(@PathVariable Long id){

        Match match = matchService.getById(id);

        if(match==null){
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> response = new HashMap<>();

        // Inside your getMatchDetails method:
        String formattedTime = "07:00 PM";
        if (match.getMatchTime() != null) {
            // If matchTime is a LocalTime object:
            formattedTime = match.getMatchTime().format(DateTimeFormatter.ofPattern("hh:mm:ss a"));
        }

        // Basic match fields
        response.put("id", match.getId());
        response.put("teamAName", match.getTeamA() != null ? match.getTeamA().getTeamName() : "TBD");
        response.put("teamBNaname", match.getTeamB() != null ? match.getTeamB().getTeamName() : "TBD");
        response.put("teamAScore", match.getTeamAScore());
        response.put("teamBScore", match.getTeamBScore());
        response.put("status", match.getStatus());
        response.put("matchTime", formattedTime);
        response.put("venueLocation", match.getVenueLocation());
        response.put("roundNumber", match.getRoundNumber());
        response.put("umpireName", match.getUmpireName());


        if (match.getTeamA() != null && match.getTeamA().getRoster() != null) {
            List<Map<String, Object>> teamAPlayers = match.getTeamA().getRoster().stream().map(rosterItem -> {
                Map<String, Object> pMap = new HashMap<>();
                
                // Extract profile and handle potential nulls safely
                var profile = rosterItem.getPlayerProfile();
                String fullName = (profile != null) ? profile.getFullName() : "Player";
                
                // Extract profile picture (update method name if your entity uses something else like getAvatar())
                String profilePic = (profile != null && profile.getProfileImage() != null) ? profile.getProfileImage() : "/images/default-avatar.png";
                
                boolean isCap = rosterItem.getRoleInTeam() != null && rosterItem.getRoleInTeam().name().equals("CAPTAIN");
                boolean isVCap = rosterItem.getRoleInTeam() != null && rosterItem.getRoleInTeam().name().equals("VICE_CAPTAIN");

                pMap.put("name", fullName);
                pMap.put("jerseyNumber", rosterItem.getJerseyNumber());
                pMap.put("profilePic", profilePic);
                pMap.put("isCaptain", isCap);
                pMap.put("isViceCaptain", isVCap);
                
                // Sorting priority: Captain = 1, Vice-Captain = 2, Others = 3
                int priority = isCap ? 1 : (isVCap ? 2 : 3);
                pMap.put("sortPriority", priority);

                return pMap;
            })
            .sorted(Comparator.comparingInt(p -> (Integer) p.get("sortPriority")))
            .collect(Collectors.toList());
            
            response.put("teamAPlayers", teamAPlayers);
        }

        // Team B Players list map
        if (match.getTeamB() != null && match.getTeamB().getRoster() != null) {
            List<Map<String, Object>> teamBPlayers = match.getTeamB().getRoster().stream().map(rosterItem -> {
                Map<String, Object> pMap = new HashMap<>();
                
                var profile = rosterItem.getPlayerProfile();
                String fullName = (profile != null) ? profile.getFullName() : "Player";
                
                // Extract profile picture
                String profilePic = (profile != null && profile.getProfileImage() != null) ? profile.getProfileImage() : "/images/default-avatar.png";
                
                boolean isCap = rosterItem.getRoleInTeam() != null && rosterItem.getRoleInTeam().name().equals("CAPTAIN");
                boolean isVCap = rosterItem.getRoleInTeam() != null && rosterItem.getRoleInTeam().name().equals("VICE_CAPTAIN");

                pMap.put("name", fullName);
                pMap.put("jerseyNumber", rosterItem.getJerseyNumber());
                pMap.put("profilePic", profilePic);
                pMap.put("isCaptain", isCap);
                pMap.put("isViceCaptain", isVCap);
                
                // Sorting priority: Captain = 1, Vice-Captain = 2, Others = 3
                int priority = isCap ? 1 : (isVCap ? 2 : 3);
                pMap.put("sortPriority", priority);

                return pMap;
            })
            .sorted(Comparator.comparingInt(p -> (Integer) p.get("sortPriority")))
            .collect(Collectors.toList());
    
            response.put("teamBPlayers", teamBPlayers);
        }

        return ResponseEntity.ok(response);
    }

    // Place the helper method right here inside the controller class
    private String formatRoundName(String roundNumber) {
        if (roundNumber == null) return "Group Stage";
        String upper = roundNumber.toUpperCase();
        if (upper.equals("ROUND_1") || upper.equals("ROUND_2") || upper.equals("ROUND_3")) {
            return "Group Stage";
        }
        // Capitalize and replace underscores for other rounds like QUARTER_FINALS
        String formatted = roundNumber.toLowerCase().replace('_', ' ');
        return Character.toUpperCase(formatted.charAt(0)) + formatted.substring(1);
    }

    @PostMapping("/{matchId}/assign-scorer")
    public ResponseEntity<?> assignScorer(
        @PathVariable("matchId") Long matchId,
        @RequestBody ScoringAccessDTO req
    ){
       ScoringAccess assignedScorer = scoringAccessService.assignScorer(matchId, req);

       ScoringAccessDTO responseDto = ScoringAccessDTO.fromEntity(assignedScorer);
       return ResponseEntity.ok(responseDto);
    }
}
