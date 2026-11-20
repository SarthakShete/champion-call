package com.champions_Call.service;

import com.champions_Call.model.Tournament.Tournament;
import com.champions_Call.model.match.Match;
import com.champions_Call.repository.MatchRepository;


import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.champions_Call.Form.organizer.NotificationDTO;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class NotificationService {

    private final MatchRepository matchRepository;

    public NotificationService(MatchRepository matchRepository) {
        this.matchRepository = matchRepository;
    }

   public List<NotificationDTO> getLiveNotificationsForOrganizer(Long organizerId) {
        List<NotificationDTO> notifications = new ArrayList<>();
        
        // 1. Get current date and time
        LocalDate today = LocalDate.now();
        LocalTime currentTime = LocalTime.now();

        // 2. Define the strict 15-minute window (e.g., exactly 14 to 16 minutes from now)
        LocalTime windowStart = currentTime.plusMinutes(14);
        LocalTime windowEnd = currentTime.plusMinutes(16);

        // 3. Fetch only matches scheduled for TODAY within this 15-minute window
        List<Match> upcomingMatches = matchRepository.findMatchesByDateAndTimeRange(today, windowStart, windowEnd);

        for (Match match : upcomingMatches) {
            // Skip completed matches just in case
            if ("COMPLETED".equals(match.getStatus())) {
                continue;
            }

            Tournament tournament = match.getTournament();
            if (tournament != null && tournament.getOrganizer() != null && tournament.getOrganizer().getId().equals(organizerId)) {
                
                String tournamentName = tournament.getTournamentName();
                String teamAName = match.getTeamA() != null ? match.getTeamA().getTeamName() : "TBD";
                String teamBName = match.getTeamB() != null ? match.getTeamB().getTeamName() : "TBD";
                String venue = match.getVenueLocation() != null ? match.getVenueLocation() : "Main Arena";

                String title = tournamentName + " - Match Alert ⏱️";
                String message = String.format("Match #%s (%s vs %s) starts in 15 minutes at %s.",
                    match.getMatchNumber() != null ? match.getMatchNumber() : match.getId(),
                    teamAName, teamBName, venue
                );
                String matchUrl = "/api/organizer/tournament/tournament_details/" + tournament.getId() + "#matchDetail";

                notifications.add(new NotificationDTO(title, message, matchUrl));
            }
        }
        return notifications;
    }
}