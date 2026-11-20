package com.champions_Call.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;

import com.champions_Call.Form.Match.ScoringAccessDTO;
import com.champions_Call.Helper.AccessDuration;
import com.champions_Call.Helper.AccessStatus;
import com.champions_Call.model.match.Match;
import com.champions_Call.model.match.ScoringAccess;
import com.champions_Call.repository.MatchRepository;
import com.champions_Call.repository.ScoringAccessRepository;
import com.champions_Call.service.EmailNotificationService;
import com.champions_Call.service.MatchService;
import com.champions_Call.service.ScoringAccessService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Service
public class ScoringAccessServiceImpl implements ScoringAccessService{

  @Autowired
  private MatchService matchService;

  @Autowired
  private ScoringAccessRepository scoringAccessRepo;

  @Autowired
  private EmailNotificationService emailService;

  @Value("${app.base-url}")
  private String baseUrl;

  private static final Logger log = LoggerFactory.getLogger(ScoringAccessServiceImpl.class);

  @Override 
  public ScoringAccess saveScorer(ScoringAccess scorer){
    return scoringAccessRepo.save(scorer);
  }



  @Override
  public ScoringAccess assignScorer(Long matchId, ScoringAccessDTO request) {

     Match match = matchService.getById(matchId);
      if (match == null) {
          throw new IllegalArgumentException("Match not found: " + matchId);
      }
      
      ScoringAccess access = new ScoringAccess();

      access.setMatch(match);
      access.setScorerName(request.getScorerName());
      access.setEmail(request.getEmail());
      access.setMobileNumber(request.getMobileNumber());
      access.setAccessDuration(request.getAccessDuration());
      access.setStatus(AccessStatus.PENDING);
      access.setMobileVerified(false);
      access.setCreatedAt(LocalDateTime.now());
      access.setAccessToken(UUID.randomUUID().toString());

      ScoringAccess saved = scoringAccessRepo.save(access);

      try{
        String email = saved.getEmail();
        System.out.println("Sending scorer mail to: " + email);
        sendAccessEmail(saved);
      } catch (Exception e) {

          log.error("Scorer email failed", e);
      }

      return saved;
  }

    
    @Override
    public void resendAccess(Long matchId) {

        // 1. Find the latest scorer assignment for this match
        ScoringAccess access = scoringAccessRepo
                .findTopByMatch_IdOrderByCreatedAtDesc(matchId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No scorer assignment found for match: " + matchId));

        // 2. Do not resend access to revoked or expired assignments
        if (access.getStatus() != AccessStatus.PENDING
                && access.getStatus() != AccessStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Cannot resend access. Current status: " + access.getStatus());
        }

        // 3. Keep the existing token for an already verified scorer.
        // For an unverified scorer, generate a replacement token.
        String oldToken = access.getAccessToken();

        if (!access.isMobileVerified()) {
            access.setAccessToken(UUID.randomUUID().toString());
        }

        try {
            // 4. Send the email using the existing email method
            sendAccessEmail(access);

            // 5. Save the updated token only after email sending succeeds
            scoringAccessRepo.save(access);

            log.info("Scorer access email resent successfully for match {}",
                    matchId);

        } catch (Exception e) {

            // Restore the previous token if the email or save operation fails
            access.setAccessToken(oldToken);

            log.error("Failed to resend scorer access email for match {}",
                    matchId, e);

            throw new IllegalStateException(
                    "Failed to resend scorer access email. Please try again.", e);
        }
    }


    private void sendAccessEmail(ScoringAccess access) {
 
      Match match = access.getMatch();
 
      String verifyLink = baseUrl + "/scorer/verify?token=" + access.getAccessToken();
      String matchTitle = buildMatchTitle(match);
      String subject = "You are assigned as scorer - " + matchTitle;
      String durationText = buildDurationText(access.getAccessDuration());
 
      String message =
          "Dear " + access.getScorerName() + ",\n\n"
        + "You have been selected as the official scorer for the following match:\n\n"
        + "Match: " + matchTitle + "\n\n"
        + durationText + "\n\n"
        + "To begin scoring, please verify your identity using the secure link below:\n"
        + verifyLink + "\n\n"
        + "You will be asked to confirm your registered mobile number and enter a one-time password (OTP).\n\n"
        + "For security reasons, please do not share this link with anyone.\n\n"
        + "Regards,\n"
        + "ChampionCall Team";
 
      log.info("Sending scorer mail to: {}", access.getEmail());
      emailService.sendScorerAccessEmail(access.getEmail(), subject, message);
  }


    private String buildMatchTitle(Match match) {
        String teamA = match.getTeamA() != null ? match.getTeamA().getTeamName() : "Team A";
        String teamB = match.getTeamB() != null ? match.getTeamB().getTeamName() : "Team B";
        return teamA + " vs " + teamB;
    }

    private String buildDurationText(AccessDuration duration) {
        if (duration == AccessDuration.TWO_HOURS) {
            return "You have been assigned as the scorer for a period of 2 hours.";
        } else if (duration == AccessDuration.SIX_HOURS) {
            return "You have been assigned as the scorer for a period of 6 hours.";
        } else if (duration == AccessDuration.TWENTY_FOUR_HOURS) {
            return "You have been assigned as the scorer for a period of 24 hours.";
        }
        return "You have been assigned as the scorer until the match is completed.";
    }

   


    @Override 
    public List<ScoringAccess> getAllScorerMatchUsingTournamentID(Long tournamentId){
        return scoringAccessRepo.findLatestAssignmentsByTournament(tournamentId);
    }

    @Override 
    public ScoringAccess getCurrentScorer(Long matchId){
        return scoringAccessRepo.findTopByMatch_IdOrderByCreatedAtDescIdDesc(matchId).orElse(null);
    }

    @Override 
    public void revokeAccess(Long matchId){
        // Find the latest scorer assignment for this match
        ScoringAccess access = scoringAccessRepo
            .findTopByMatch_IdOrderByCreatedAtDesc(matchId)
            .orElseThrow(() -> new IllegalArgumentException(
                    "No scorer assignment found for match: " + matchId));

        // Allow revocation only for pending or active assignments
        if (access.getStatus() != AccessStatus.PENDING
                && access.getStatus() != AccessStatus.ACTIVE) {

            throw new IllegalStateException(
                    "Cannot revoke scorer access. Current status: "
                            + access.getStatus());
        }

        // Update the existing assignment
        access.setStatus(AccessStatus.REVOKED);
        access.setRevokedAt(LocalDateTime.now());

        // Save changes to the database
        scoringAccessRepo.save(access);

        log.info("Scorer access revoked for match {}", matchId);
    }
}
