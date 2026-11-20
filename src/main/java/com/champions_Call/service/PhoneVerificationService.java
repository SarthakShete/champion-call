package com.champions_Call.service;

public interface PhoneVerificationService {

  public void sendVerificationCode(String phoneNumber);

  public boolean verifyCode(String phoneNumber, String code);

}
