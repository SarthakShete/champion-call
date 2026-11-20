
package com.champions_Call.model.Manging_Scoring.Football;

import com.champions_Call.model.match.Match;

import jakarta.persistence.*;

@Entity
@Table(name = "football_matches")
public class FootballMatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false, unique = true)
    private Match match;

    @Column(name = "current_half")
    private int currentHalf = 1;

    @Column(name = "first_half_duration")
    private int firstHalfDuration = 45;

    @Column(name = "second_half_duration")
    private int secondHalfDuration = 45;

    @Column(name = "home_team_score")
    private int homeTeamScore = 0;

    @Column(name = "away_team_score")
    private int awayTeamScore = 0;

    @Column(name = "extra_time_played")
    private boolean extraTimePlayed = false;

    @Column(name = "penalties_played")
    private boolean penaltiesPlayed = false;

    public FootballMatch() {
    }

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

    public int getCurrentHalf() {
        return currentHalf;
    }

    public void setCurrentHalf(int currentHalf) {
        this.currentHalf = currentHalf;
    }

    public int getFirstHalfDuration() {
        return firstHalfDuration;
    }

    public void setFirstHalfDuration(int firstHalfDuration) {
        this.firstHalfDuration = firstHalfDuration;
    }

    public int getSecondHalfDuration() {
        return secondHalfDuration;
    }

    public void setSecondHalfDuration(int secondHalfDuration) {
        this.secondHalfDuration = secondHalfDuration;
    }

    public int getHomeTeamScore() {
        return homeTeamScore;
    }

    public void setHomeTeamScore(int homeTeamScore) {
        this.homeTeamScore = homeTeamScore;
    }

    public int getAwayTeamScore() {
        return awayTeamScore;
    }

    public void setAwayTeamScore(int awayTeamScore) {
        this.awayTeamScore = awayTeamScore;
    }

    public boolean isExtraTimePlayed() {
        return extraTimePlayed;
    }

    public void setExtraTimePlayed(boolean extraTimePlayed) {
        this.extraTimePlayed = extraTimePlayed;
    }

    public boolean isPenaltiesPlayed() {
        return penaltiesPlayed;
    }

    public void setPenaltiesPlayed(boolean penaltiesPlayed) {
        this.penaltiesPlayed = penaltiesPlayed;
    }
}
