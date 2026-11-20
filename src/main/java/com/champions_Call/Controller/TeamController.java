package com.champions_Call.Controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.champions_Call.Form.Team.JoinTeamRequestDto;
import com.champions_Call.Form.Team.RosterMemberDTO;
import com.champions_Call.model.User;
import com.champions_Call.model.Team.TeamRegistration;
import com.champions_Call.model.Team.TeamRoster;
import com.champions_Call.model.player.PlayerProfile;
import com.champions_Call.service.ImageService;
import com.champions_Call.service.PlayerService;
import com.champions_Call.service.SecurityUtils;
import com.champions_Call.service.TeamRegistrationService;
import com.champions_Call.service.TeamRosterService;

@RestController
@RequestMapping("/api/teams")
public class TeamController {

  @Autowired 
    private PlayerService playerService;

    @Autowired
    private SecurityUtils securityUtils;

    @Autowired
    private ImageService imageService;

    @Autowired
    private PlayerService playerProfileService;

    @Autowired
    private TeamRegistrationService teamRegistrationService;

    @Autowired
    private TeamRosterService teamRosterService;


    @PostMapping("/join")
    public ResponseEntity<?> joinTeam(@RequestBody JoinTeamRequestDto joinTeamReqDTO) {
        try {
            User user = securityUtils.getLoggedInUser();

            if (user == null) {
                return ResponseEntity.status(401).body("User not Authenticated");
            }

            // 1. Validate that the player has completed their profile / submitted ID proof
            PlayerProfile profile = playerProfileService.getPlayerProfileByUser(user).orElse(null); // Adjust to match your service method to fetch profile

            System.out.println("The Profile is before Id : "+profile.getFullName());
            if (profile == null || (profile.getPlayerIdProof() == null || profile.getPlayerIdProof().trim().isEmpty())) {
                // Return structured JSON with error message and specific redirect URL
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "You must submit and upload your ID proof before joining a team!",
                    "redirectUrl", "/api/player/profile#profile-view"
                ));
            }

            // 1. Fetch the team registration to check squad limits
            TeamRegistration team = teamRegistrationService.getTeamRegistrationById(joinTeamReqDTO.getTeamRegistrationId())
                .orElseThrow(() -> new RuntimeException("Team not found"));

            // 2. Validate max squad size directly in the controller
            int maxSquadSize = team.getTournament().getMaxSquadSize() != null ? team.getTournament().getMaxSquadSize() : 10;
           
            // FIX: count the real roster rows instead of team.getSquadSize().
            // registerTeam saved squadSize = tournament max (12), so the old check
            // (12 >= 12) rejected every joiner even though the team had 1 member.
            int currentSquadSize = teamRosterService.getByTeamRegistrationId(team.getId()).size();

            if (currentSquadSize >= maxSquadSize) {
                return ResponseEntity.badRequest().body("Cannot join! Team squad is full (Maximum allowed: " + maxSquadSize + ")");
            }

            String email = user.getEmail();

            // 3. Proceed with joining the team
            teamRegistrationService.joinTeamWithCode(email, joinTeamReqDTO.getTeamRegistrationId(), joinTeamReqDTO.getInviteCode());

            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("message", "Successfully joined the team!");
            resp.put("redirectUrl", "/api/player/profile");

            return ResponseEntity.ok(resp);
        } 
        catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());    
        }
    }

   

    @GetMapping("/get-roster")
    public ResponseEntity<List<RosterMemberDTO>> getTeamRoster(@RequestParam Long teamRegId) {

        // Fetch all roster rows (members) tied to this team registration
        List<TeamRoster> roster = teamRosterService.getByTeamRegistrationId(teamRegId);

        System.out.println("The roster size is : " + roster.size());

        // FIX: We no longer return the TeamRoster entities directly.
        // The entities point to each other in a loop
        // (TeamRoster -> TeamRegistration -> roster -> TeamRoster, and
        // PlayerProfile -> User -> PlayerProfile). Jackson followed that loop
        // until it exceeded the max nesting depth (1000), so the JSON broke and
        // the popup showed "Failed to load team squad details".
        // Copying only the fields the UI needs into a flat DTO avoids the loop.
        // It also stops the User object (including the password hash) from
        // being sent to the browser.
        List<RosterMemberDTO> result = roster.stream()
            .map(r -> new RosterMemberDTO(
                r.getId(),                                   // roster row id
                // Send the role as a plain string; default to PLAYER if not set
                r.getRoleInTeam() != null ? r.getRoleInTeam().name() : "PLAYER",
                r.getPlayerProfile().getId(),                // used to open the profile popup
                r.getPlayerProfile().getFullName(),          // name shown in the squad list
                r.getPlayerProfile().getProfileImage()))     // avatar shown in the squad list
            .toList();

        return ResponseEntity.ok(result);
    }
    
    @PostMapping("/update-role")
    public ResponseEntity<?> updateTeamMemeberRole(
        @RequestParam Long teamRegId,
        @RequestParam Long rosterId,
        @RequestParam String action
    ){
        try{
            teamRegistrationService.processTeamAction(teamRegId,rosterId,action);

            return ResponseEntity.ok().body("Successfully updated team member role.");
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/update-name")
    public ResponseEntity<?> updateTeamName(
        @RequestParam("teamRegId") Long teamRegId,
        @RequestParam("newName") String newName
    ){

        System.out.println("The update - name is called...");
        System.out.println("The update - name is : "+newName);

        try{
            TeamRegistration teamReg = teamRegistrationService.getTeamRegistrationById(teamRegId).orElse(null);

            if(teamReg==null){  
                throw new RuntimeException("Team registration not found.");
            }

            teamReg.setTeamName(newName);
            teamRegistrationService.saveTeam(teamReg);

           return ResponseEntity.ok().body("Team name updated successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
