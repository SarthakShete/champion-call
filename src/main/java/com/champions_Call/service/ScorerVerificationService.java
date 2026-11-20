package com.champions_Call.service;

import com.champions_Call.Form.ScorerVerify.ScorerLinkDetailsResponseDTO;
import com.champions_Call.Form.ScorerVerify.ScorerSessionResponseDTO;
import com.champions_Call.model.match.ScoringAccess;

public interface ScorerVerificationService {

    // Step A: verify page opens. Check the link and return match title + masked mobile.
    ScorerLinkDetailsResponseDTO getLinkDetails(String token);

    // Step B: scorer enters mobile number. If it matches, generate and send the OTP.
    void sendOtp(String token, String mobileNumber);

    // Step C: scorer enters the OTP. If correct, activate access and return a session token.
    ScorerSessionResponseDTO verifyOtp(String token, String otp);

    // Used by every scoring API: check the session token is valid for this match.
    ScoringAccess validateSession(String sessionToken, Long matchId);
}
