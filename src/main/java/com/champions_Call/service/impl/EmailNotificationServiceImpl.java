package com.champions_Call.service.impl;

import java.util.List;

import com.champions_Call.service.EmailNotificationService;

import org.springframework.beans.factory.annotation.Autowired;

import com.champions_Call.model.Team.TeamRegistration;
import com.champions_Call.model.Tournament.Tournament;
import com.champions_Call.repository.TeamRegistrationRepository;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import com.champions_Call.service.TeamRegistrationService;
import com.champions_Call.service.TournamentService;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailNotificationServiceImpl implements EmailNotificationService{


  @Autowired
  private TeamRegistrationService teamRegistrationService;

  @Autowired
  private JavaMailSender javaMailSender;

  @Autowired
  private TournamentService tournamentService;
  

  @Async
  public void sendSchedulePublishedEmailToCaptains(Long tournamentId, String organizerName, String organizerEmail){
      
    // fetch all teams which is Registred for this tournament

    Tournament tournament = tournamentService.getTournamentId(tournamentId);

    List<TeamRegistration> registrations = teamRegistrationService.getTeamsByTournamentId(tournamentId);

    for(TeamRegistration registration : registrations){

      String recipientEmail = registration.getEmail();
      String captainName = registration.getCaptainName();
      String teamName = registration.getTeamName();

      if (recipientEmail != null && !recipientEmail.trim().isEmpty()) {
          sendEmail(recipientEmail, captainName, organizerName,organizerEmail,teamName);
      }
    }
  }

  
  public void sendEmail(String recipientEmail,String captainName,String organizerName,String organizerEmail,String teamName){
      try {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

        String profileLink = "http://localhost:8080/api/player/profile";

        // Embed the organizer's actual name and contact email clearly inside the text body
        String emailBody = String.format(
            "Hello %s,\n\n" +
            "The match schedule for your team '%s' has been officially published.\n\n" +
            "You can view your match dates, times, and venue locations directly by visiting your profile page here:\n" +
            "%s\n\n" +
            "If you have any questions or queries regarding the schedule, please contact the organizer directly at: %s\n\n" +
            "Best regards,\n" +
            "%s\n" +
            "ChampionCall Organizer Team",
            captainName != null ? captainName : "Captain",
            teamName != null ? teamName : "your team",
            profileLink,
            organizerEmail,
            organizerName
        );

        helper.setTo(recipientEmail);
        helper.setSubject("Schedule Published! - " + teamName);
        helper.setText(emailBody);
        
        // This will show Sarthak's server configuration as the sender channel, but clearly attribute the event to the organizer
        helper.setFrom("sarthakshete745@gmail.com", organizerName);

        javaMailSender.send(mimeMessage);
        System.out.println("Email sent successfully to: " + recipientEmail);
        
    } catch (Exception e) {
        System.err.println("Failed to send email to " + recipientEmail + ": " + e.getMessage());
    }
  }

  @Override
  public void sendScorerAccessEmail(String toEmail, String subject, String message) {
      if (toEmail == null || toEmail.isBlank()) return;

      SimpleMailMessage mail = new SimpleMailMessage();
      System.out.println("Sending email from Email Service : "+toEmail);
      mail.setTo(toEmail);
      mail.setSubject(subject);
      mail.setText(message);
      javaMailSender.send(mail);
  }
}
