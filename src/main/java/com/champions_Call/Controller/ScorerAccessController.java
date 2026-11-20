package com.champions_Call.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.HttpStatus;

import com.champions_Call.Form.Match.ScoringAccessDTO;
import com.champions_Call.Form.ScorerVerify.ScorerLinkDetailsResponseDTO;
import com.champions_Call.Form.ScorerVerify.ScorerSendOtpRequestDTO;
import com.champions_Call.Form.ScorerVerify.ScorerSessionResponseDTO;
import com.champions_Call.Form.ScorerVerify.ScorerVerifyOtpRequestDTO;
import com.champions_Call.Helper.AccessStatus;
import com.champions_Call.model.User;
import com.champions_Call.model.match.Match;
import com.champions_Call.model.match.ScoringAccess;
import com.champions_Call.model.organizer.OrganizerProfile;
import com.champions_Call.service.MatchService;
import com.champions_Call.service.OrganizerService;
import com.champions_Call.service.ScorerVerificationService;
import com.champions_Call.service.ScoringAccessService;
import com.champions_Call.service.SecurityUtils;

import jakarta.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.ui.Model;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Controller
@RequestMapping("/scorer")
public class ScorerAccessController {


  @Autowired
  private ScorerVerificationService scorerVerificationService;

  @Autowired 
  private ScoringAccessService scoringAccessService;

  @Autowired
private SecurityUtils securityUtils;

  @Autowired
    private OrganizerService organizerService;

    @Autowired 
    private MatchService matchService;

  @GetMapping("/verify")
  public String verify(@RequestParam(required = false) String token,
                       HttpSession httpSession){

    Object savedToken = httpSession.getAttribute("SCORER_TOKEN");
    Object savedMatchId = httpSession.getAttribute("SCORER_MATCH_ID");

    // already verified in this browser with this same link
    if (token != null && token.equals(savedToken) && savedMatchId != null) {
        return "redirect:/scorer/match/" + savedMatchId;
    }

    // not verified yet, so show the verify page
    return "player/scorer-verify";
  }

  // API 1: GET /scorer/link/{token}
  @GetMapping("/link/{token}")
  public ResponseEntity<?> getLinkDetails(@PathVariable("token") String token) {
      try {
          ScorerLinkDetailsResponseDTO details = scorerVerificationService.getLinkDetails(token);
          return ResponseEntity.ok(details);
      } catch (Exception e) {
          e.printStackTrace();
          return errorResponse();
      }
  }

   // API 2: POST /scorer/send-otp
  @PostMapping("/send-otp")
  public ResponseEntity<?> sendOtp(@RequestBody ScorerSendOtpRequestDTO req) {
      try {
          scorerVerificationService.sendOtp(req.token(), req.mobileNumber());
          return ResponseEntity.ok(Map.of("message", "OTP sent to your registered email"));
      } catch (Exception e) {
          e.printStackTrace();
          return errorResponse();
      }
  }

  // API 3: POST /scorer/verify-otp
  @PostMapping("/verify-otp")
  public ResponseEntity<?> verifyOtp(@RequestBody ScorerVerifyOtpRequestDTO req, HttpSession httpSession) {
      try {
          ScorerSessionResponseDTO session = scorerVerificationService.verifyOtp(req.token(), req.otp());

          httpSession.setAttribute("SCORER_MATCH_ID", session.matchId());
          httpSession.setAttribute("SCORER_TOKEN", req.token());

          return ResponseEntity.ok(session);
      } catch (Exception e) {
          e.printStackTrace();
          return errorResponse();
      }
  }

  @GetMapping("/scorer-assignments")
  public ResponseEntity<List<ScoringAccessDTO>> getScorerAssignment(@RequestParam  Long tournamentId){

        List<ScoringAccess> assignments = scoringAccessService.getAllScorerMatchUsingTournamentID(tournamentId);


        List<ScoringAccessDTO> result = new ArrayList<>();

        for (ScoringAccess access : assignments) {

            ScoringAccessDTO dto = new ScoringAccessDTO();

            dto.setId(access.getId());
            dto.setMatchId(access.getMatch().getId());
            dto.setScorerName(access.getScorerName());
            dto.setEmail(access.getEmail());
            dto.setAccessDuration(access.getAccessDuration());
            dto.setStatus(access.getStatus());
            dto.setMobileVerified(access.isMobileVerified());
            dto.setCreatedAt(access.getCreatedAt());
            dto.setVerifiedAt(access.getVerifiedAt());
            dto.setRevokedAt(access.getRevokedAt());

            result.add(dto);
        }

        return ResponseEntity.ok(result);
    }

    @PostMapping("/{matchId}/replace-scorer")
    public ResponseEntity<String> replaceScorer(@PathVariable Long matchId){
        
        ScoringAccess currentScorer = scoringAccessService.getCurrentScorer(matchId);

        currentScorer.setStatus(AccessStatus.REVOKED);
        currentScorer.setRevokedAt(LocalDateTime.now());

        scoringAccessService.saveScorer(currentScorer);

        return ResponseEntity.ok(
            "Current scorer access revoked. You can now assign a new scorer."
        );
    }

    @PostMapping("/{matchId}/resend-scorer-access")
    public ResponseEntity<String> resendScorerAccess(@PathVariable Long matchId){
        scoringAccessService.resendAccess(matchId);

        return ResponseEntity.ok("Scorer access Email resend Successfully...");

    }

    @PostMapping("/{matchId}/assign-scorer")
    public ResponseEntity<?> assignScorer(
            @PathVariable Long matchId,
            @RequestBody ScoringAccessDTO request) {

        ScoringAccess saved =
                scoringAccessService.assignScorer(matchId, request);

        
       ScoringAccessDTO response = new ScoringAccessDTO();
        response.setId(saved.getId());
        response.setMatchId(saved.getMatch().getId());
        response.setScorerName(saved.getScorerName());
        response.setEmail(saved.getEmail());
        response.setAccessDuration(saved.getAccessDuration());
        response.setStatus(saved.getStatus());
        response.setMobileVerified(saved.isMobileVerified());
        response.setCreatedAt(saved.getCreatedAt());


        return ResponseEntity.ok(response);
    }

    @PutMapping("/{matchId}/revoke-scorer")
    public ResponseEntity<?> revokeScorerAccess(@PathVariable Long matchId){
        
        scoringAccessService.revokeAccess(matchId);

        System.out.println("The revoke Scorer method calleeddd");

        return ResponseEntity.ok(
                java.util.Map.of(
                        "message", "Scorer access revoked successfully",
                        "status", "REVOKED"
                )
        );
    }

    @GetMapping("/managing-score")
    public String managingScore(@RequestParam Long matchId,Model model){

        User user = securityUtils.getLoggedInUser();
        
        if (user == null) {
            return "redirect:/login";
        }

        
        OrganizerProfile organizerProfile = organizerService.getByUser(user);
        model.addAttribute("matchId", matchId);
        model.addAttribute("scorerName",organizerProfile.getOrganizationName());

        return "fragments/Managing_Scoring";
    }


    // One common error for every problem
    private ResponseEntity<?> errorResponse() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("message", "Verification failed"));
    }

    // Redirect Scorer to managing Scorer Page...
    @GetMapping("/match/{matchId}")
    public String scorerPage(@PathVariable Long matchId,
                            HttpSession httpSession,
                            Model model) {

        Object allowed = httpSession.getAttribute("SCORER_MATCH_ID");
        if (allowed == null || !allowed.equals(matchId)) {
            return "redirect:/scorer/verify";
        }

        ScoringAccess current = scoringAccessService.getCurrentScorer(matchId);
        if (current == null || current.getStatus() == AccessStatus.REVOKED) {
            return "redirect:/scorer/verify";
        }

        Match match = matchService.getById(matchId);
        String matchName = match.getTeamA()+ " vs " + match.getTeamB();

        model.addAttribute("matchId", matchId);
        model.addAttribute("matchName", matchName);
        model.addAttribute("scorerName", current.getScorerName());

        return "player/scorer-scoreboard";
    }
}
