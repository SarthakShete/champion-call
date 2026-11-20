package com.champions_Call.model.Manging_Scoring.Cricket;

import com.champions_Call.model.match.Match;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "cricket_matches")
public class CricketMatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Link with existing Match entity
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false, unique = true)
    private Match match;

    // Toss information
    @Column(name = "toss_winner_team_id")
    private Long tossWinnerTeamId;

    @Enumerated(EnumType.STRING)
    @Column(name = "toss_decision")
    private TossDecision tossDecision;

    // Innings information
    @Column(name = "current_innings_number")
    private int currentInningsNumber = 1;

    @Column(name = "total_innings")
    private int totalInnings = 2;

    // Constructors
    public CricketMatch() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Match getMatch() {
        return match;
    }

    public void setMatch(Match match) {
        this.match = match;
    }

    public Long getTossWinnerTeamId() {
        return tossWinnerTeamId;
    }

    public void setTossWinnerTeamId(Long tossWinnerTeamId) {
        this.tossWinnerTeamId = tossWinnerTeamId;
    }

    public TossDecision getTossDecision() {
        return tossDecision;
    }

    public void setTossDecision(TossDecision tossDecision) {
        this.tossDecision = tossDecision;
    }

    public int getCurrentInningsNumber() {
        return currentInningsNumber;
    }

    public void setCurrentInningsNumber(int currentInningsNumber) {
        this.currentInningsNumber = currentInningsNumber;
    }

    public int getTotalInnings() {
        return totalInnings;
    }

    public void setTotalInnings(int totalInnings) {
        this.totalInnings = totalInnings;
    }

    public enum TossDecision {
        BAT,
        BOWL
    }
}
