package com.champions_Call.service;

public interface EmailNotificationService {


    void sendSchedulePublishedEmailToCaptains(Long tournamentId, String organizerName, String organizerEmail);
    
    void sendEmail(String recipientEmail, String captainName, String organizerName, String organizerEmail, String teamName);


    void sendScorerAccessEmail(String toEmail, String subject, String message);
}
