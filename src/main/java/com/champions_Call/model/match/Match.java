package com.champions_Call.model.match;

import com.champions_Call.model.Team.TeamRegistration;
import com.champions_Call.model.Tournament.Tournament;
import com.twilio.twiml.voice.Prompt.For;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

import com.champions_Call.Helper.MatchStatus;


@Entity
@Table(name = "matches")
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id", nullable = false)
    private Tournament tournament;

    @Column(name = "round_number", nullable = false)
    private String roundNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_a_id")
    private TeamRegistration teamA;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_b_id")
    private TeamRegistration teamB;

    @Column(name = "team_a_score")
    private Integer teamAScore = 0;

    @Column(name = "team_b_score")
    private Integer teamBScore = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchStatus status = MatchStatus.SCHEDULED; // SCHEDULED, BYE, COMPLETED, etc.

    // 🕒 Dedicated scheduling fields for time and venue
    @Column(name = "match_date")
    private LocalDate matchDate; // e.g., 2026-07-25

    @Column(name = "match_time")
    private LocalTime matchTime; // e.g., 15:30:00

    @Column(name = "venue_location")
    private String venueLocation; // e.g., "Ground A, Pune"

    @Column(name = "umpire_name")
    private String umpireName;

    @Transient
    private boolean hasConflict;

    @Transient
    private String conflictMessage;

    // Getters and Setters
    public boolean isHasConflict() { return hasConflict; }
    public void setHasConflict(boolean hasConflict) { this.hasConflict = hasConflict; }

    public String getConflictMessage() { return conflictMessage; }
    public void setConflictMessage(String conflictMessage) { this.conflictMessage = conflictMessage; }

    // dependsOnMatchNumber tells the system where a team in the current match comes from.
    // Suppose we have these matches:
    // Match 1: Team A vs Team B
    // Match 2: Team C vs Team D
    // Match 3: Winner of Match 1 vs Winner of Match 2

    // For Match 3, we don't know the teams yet. They will be decided after Match 1 and Match 2 are completed.

    // So we store:
    //     Match 3
    //     dependsOnMatchNumber = 1

    // This means: “One of the teams in Match 3 comes from Match 1.”

    // If Match 3 also depends on Match 2, you might have another dependency field, such as:

    // Match 3 : dependsOnMatchNumber = 1

    // and another field for the second slot.
    private Integer dependsOnMatchNumber;

    public Integer getDependsOnMatchNumber() {
        return dependsOnMatchNumber;
    }

    public void setDependsOnMatchNumber(Integer dependsOnMatchNumber) {
        this.dependsOnMatchNumber = dependsOnMatchNumber;
    }
   

    // Default Constructor
    public Match() {
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Tournament getTournament() { return tournament; }
    public void setTournament(Tournament tournament) { this.tournament = tournament; }

    public String getRoundNumber() { return roundNumber; }
    public void setRoundNumber(String roundNumber) { this.roundNumber = roundNumber; }

    public TeamRegistration getTeamA() { return teamA; }
    public void setTeamA(TeamRegistration teamA) { this.teamA = teamA; }

    public TeamRegistration getTeamB() { return teamB; }
    public void setTeamB(TeamRegistration teamB) { this.teamB = teamB; }

    public Integer getTeamAScore() { return teamAScore; }
    public void setTeamAScore(Integer teamAScore) { this.teamAScore = teamAScore; }

    public Integer getTeamBScore() { return teamBScore; }
    public void setTeamBScore(Integer teamBScore) { this.teamBScore = teamBScore; }

    public MatchStatus getStatus() { 
        return status; 
    }
    
    public void setStatus(MatchStatus status) { 
        this.status = status; 
    }
    public LocalDate getMatchDate() { return matchDate; }
    public void setMatchDate(LocalDate matchDate) { this.matchDate = matchDate; }

    public LocalTime getMatchTime() { return matchTime; }
    public void setMatchTime(LocalTime matchTime) { this.matchTime = matchTime; }

    public String getVenueLocation() { return venueLocation; }
    public void setVenueLocation(String venueLocation) { this.venueLocation = venueLocation; }

    public Long getMatchNumber() {
        return this.id; // or return your specific matchNumber field
    }

    public String getUmpireName() {
        return umpireName;
    }

    public void setUmpireName(String umpireName) {
        this.umpireName = umpireName;
    }
    public Match orElseThrow(Object object) {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException("Unimplemented method 'orElseThrow'");
    }
}