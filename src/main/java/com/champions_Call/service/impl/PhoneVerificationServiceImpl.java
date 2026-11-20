package com.champions_Call.service.impl;

import com.champions_Call.service.PhoneVerificationService;
import com.twilio.Twilio;
import com.twilio.rest.verify.v2.service.VerificationCheck;
import jakarta.annotation.PostConstruct;
import com.twilio.rest.verify.v2.service.Verification;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PhoneVerificationServiceImpl implements PhoneVerificationService{

  @Value("${twilio.account.sid}")
  private String accountSid;

  @Value("${twilio.auth.token}")
  private String  authToken;

  @Value("${twilio.verify.service.sid}")
  private String verifyServiceSid;

  @PostConstruct
    public void initTwilio() {
        // Initializes the Twilio client execution instance when Spring starts up
      Twilio.init(accountSid, authToken);
  }

  @Override
  public void sendVerificationCode(String phoneNumber){
    // Triggers the Twilio verification machine engine to send a text
    Verification.creator(
      verifyServiceSid,
      phoneNumber, // Needs to be E.164 format (+91XXXXXXXXXX)
      "sms"
    ).create();
  }

  @Override
    public boolean verifyCode(String phoneNumber, String code) {
        try {
            // Checks the code against the active verification pipeline instance
            VerificationCheck check = VerificationCheck.creator(verifyServiceSid)
                    .setCode(code)
                    .setTo(phoneNumber)
                    .create();
            
            return "approved".equals(check.getStatus());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
