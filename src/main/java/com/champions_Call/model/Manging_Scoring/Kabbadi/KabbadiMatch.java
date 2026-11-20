
package com.champions_Call.model.Manging_Scoring.Kabbadi;

import com.champions_Call.model.match.Match;

import jakarta.persistence.*;

@Entity
@Table(name = "kabaddi_matches")
public class KabbadiMatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false, unique = true)
    private Match match;

    @Column(name = "current_half")
    private int currentHalf = 1;

    @Column(name = "first_half_duration")
    private int firstHalfDuration = 20;

    @Column(name = "second_half_duration")
    private int secondHalfDuration = 20;

    @Column(name = "team_a_score")
    private int teamAScore = 0;

    @Column(name = "team_b_score")
    private int teamBScore = 0;

    @Column(name = "team_a_total_raids")
    private int teamATotalRaids = 0;

    @Column(name = "team_b_total_raids")
    private int teamBTotalRaids = 0;

    @Column(name = "team_a_total_tackles")
    private int teamATotalTackles = 0;

    @Column(name = "team_b_total_tackles")
    private int teamBTotalTackles = 0;

    public KabbadiMatch() {
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

    public int getTeamAScore() {
        return teamAScore;
    }

    public void setTeamAScore(int teamAScore) {
        this.teamAScore = teamAScore;
    }

    public int getTeamBScore() {
        return teamBScore;
    }

    public void setTeamBScore(int teamBScore) {
        this.teamBScore = teamBScore;
    }

    public int getTeamATotalRaids() {
        return teamATotalRaids;
    }

    public void setTeamATotalRaids(int teamATotalRaids) {
        this.teamATotalRaids = teamATotalRaids;
    }

    public int getTeamBTotalRaids() {
        return teamBTotalRaids;
    }

    public void setTeamBTotalRaids(int teamBTotalRaids) {
        this.teamBTotalRaids = teamBTotalRaids;
    }

    public int getTeamATotalTackles() {
        return teamATotalTackles;
    }

    public void setTeamATotalTackles(int teamATotalTackles) {
        this.teamATotalTackles = teamATotalTackles;
    }

    public int getTeamBTotalTackles() {
        return teamBTotalTackles;
    }

    public void setTeamBTotalTackles(int teamBTotalTackles) {
        this.teamBTotalTackles = teamBTotalTackles;
    }
}
