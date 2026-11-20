package com.champions_Call.service.impl;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.champions_Call.Form.ScorerVerify.ScorerLinkDetailsResponseDTO;
import com.champions_Call.Form.ScorerVerify.ScorerSessionResponseDTO;
import com.champions_Call.Helper.AccessDuration;
import com.champions_Call.Helper.AccessStatus;
import com.champions_Call.model.match.ScoringAccess;
import com.champions_Call.repository.ScoringAccessRepository;
import com.champions_Call.service.ScorerVerificationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.champions_Call.service.PhoneVerificationService;

import com.champions_Call.model.match.Match;
import com.champions_Call.service.EmailNotificationService;

@Service
public class ScorerVerificationServiceImpl implements ScorerVerificationService {

    // After 5 wrong tries, the link is blocked
    private static final int MAX_ATTEMPTS  = 5;

    @Autowired
    private ScoringAccessRepository scoringAccessRepo;


    @Autowired
    private PhoneVerificationService phoneVerificationService;

    private final SecureRandom random = new SecureRandom();



    @Override
    @Transactional(readOnly = true)   // needed because Match is lazy-loaded
    public ScorerLinkDetailsResponseDTO getLinkDetails(String token) {

        // it's a function..
        // 1. find the access row and check the link is still usable
        ScoringAccess access = findValid(token);

        // 2. Hide most of the mobile number, example: ******3210
        String mobile = access.getMobileNumber();
        String masked = "******" + mobile.substring(mobile.length() - 4);

        // 3. Send match title and masked mobile to the page
        return new ScorerLinkDetailsResponseDTO(buildMatchTitle(access.getMatch()), masked);
    }

    // STEP B: scorer enters mobile number
    @Override
    public void sendOtp(String token, String mobileNumber) {

        // 1. Check the link
        ScoringAccess access = findValid(token);

        // 2.compare with the number the organizer entered
        if(mobileNumber==null || !access.getMobileNumber().equals(mobileNumber.trim())){

            // wrong number count the attempts and stop
            access.setFailedAttempts(access.getFailedAttempts() + 1);
            scoringAccessRepo.save(access);
            throw fail();
        }

        // 3. Make a 6-digit OTP (100000 to 999999)
        int number = new SecureRandom().nextInt(900000) + 100000;
        String otp = String.valueOf(number);

        // 4. Save the OTP for 5 minutes
        //    (the field is named otpHash, but for now we save the plain OTP in it)
        access.setOtpHash(otp);
        access.setOtpExpiresAt(LocalDateTime.now().plusMinutes(5));
        scoringAccessRepo.save(access);


        // 5. Email the OTP to the scorer
        String message = "Your one-time password (OTP) is: " + otp + "\n\n"
                + "It is valid for 5 minutes.\n"
                + "Do not share this code with anyone.\n\n"
                + "Regards,\n"
                + "ChampionCall Team";

        // 3. Twilio makes the OTP and sends the SMS.
        //    We always use the number saved in the database.
        try {
            phoneVerificationService.sendVerificationCode(toE164(access.getMobileNumber()));
        } catch (Exception e) {
            e.printStackTrace();   // shows the real Twilio error in the console
            throw fail();
        }   

    }

    private String toE164(String mobile) {
        return "+91" + mobile;
    }

    // STEP C: scorer enters the OTP
    @Override
    public ScorerSessionResponseDTO verifyOtp(String token, String otp) {

         // 1. Check the link
        ScoringAccess access = findValid(token);

        // 2. Ask Twilio if the OTP is correct (use the number from the database)
        boolean otpOk = false;
        if (otp != null && !otp.trim().isEmpty()) {
            otpOk = phoneVerificationService.verifyCode(
                    toE164(access.getMobileNumber()), otp.trim());
        }

        if (!otpOk) {
            // Wrong or expired OTP: count the attempt and stop
            access.setFailedAttempts(access.getFailedAttempts() + 1);
            scoringAccessRepo.save(access);
            throw fail();
        }

        // 3. OTP is correct: activate the access
        access.setStatus(AccessStatus.ACTIVE);
        access.setMobileVerified(true);
        access.setVerifiedAt(LocalDateTime.now());
        access.setFailedAttempts(0);

        // 4. Create the session token and set when the access ends
        access.setSessionToken(UUID.randomUUID().toString());
        access.setExpiresAt(calculateExpiry(access.getAccessDuration()));
        scoringAccessRepo.save(access);

        // 5. Send the session token to the scorer's browser
        return new ScorerSessionResponseDTO(access.getSessionToken(), access.getMatch().getId());
    }

    // Used by every scoring API
    @Override
    public ScoringAccess validateSession(String sessionToken, Long matchId) {
        
        // 1. Find the scorer's session
        ScoringAccess access = scoringAccessRepo
                .findBySessionToken(sessionToken)
                .orElse(null);

        if (access == null) {
            throw fail();
        }

        // 2. Check whether this is the latest assignment
        ScoringAccess latestAccess = scoringAccessRepo
                .findTopByMatch_IdOrderByCreatedAtDescIdDesc(matchId)
                .orElse(null);

        if (latestAccess == null ||
                !latestAccess.getId().equals(access.getId())) {
            throw fail();
        }

        // 3. Check access status and OTP verification
        if (access.getStatus() != AccessStatus.ACTIVE ||
                !access.isMobileVerified()) {
            throw fail();
        }

        // 4. Verify that this session belongs to the requested match
        if (!access.getMatch().getId().equals(matchId)) {
            throw fail();
        }

        // 5. Check expiry
        if (access.getExpiresAt() != null &&
                !access.getExpiresAt().isAfter(LocalDateTime.now())) {
            throw fail();
        }

        return access;
    }

    // Helper Method 

    // Work out when the access should end
    private LocalDateTime calculateExpiry(AccessDuration duration) {
        LocalDateTime now = LocalDateTime.now();

        if (duration == AccessDuration.TWO_HOURS) {
            return now.plusHours(2);
        } else if (duration == AccessDuration.SIX_HOURS) {
            return now.plusHours(6);
        } else if (duration == AccessDuration.TWENTY_FOUR_HOURS) {
            return now.plusHours(24);
        }
        return null;   // UNTIL_MATCH_ENDS: no fixed time
    }

    private ScoringAccess findValid(String token){

        // 1. Find the row. If nothing is found, stop.
        ScoringAccess access = scoringAccessRepo.findByAccessToken(token).orElse(null);
        if (access == null) {
            throw fail();
        }

        // 2. Revoked by the organizer?
        if (access.getStatus() == AccessStatus.REVOKED) {
            throw fail();
        }

        // 3. Too many wrong tries?
        if (access.getFailedAttempts() >= MAX_ATTEMPTS) {
            throw fail();
        }

        // 4. Access time already ended?
        if (access.getExpiresAt() != null && access.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw fail();
        }

        return access;

    }

    // One common error message for every problem
    private RuntimeException fail() {
        return new RuntimeException("Verification failed");
    }


    // Example: "Malvani Strikers vs Kalyan Blasters"
    private String buildMatchTitle(Match match) {
        // Use the SAME team getters you use in your email code
        return match.getTeamA().getTeamName() + " vs " + match.getTeamB().getTeamName();
    }

    
}
