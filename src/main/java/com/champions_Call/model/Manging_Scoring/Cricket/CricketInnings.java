
package com.champions_Call.model.Manging_Scoring.Cricket;

import com.champions_Call.model.Team.TeamRegistration;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "cricket_innings",
    uniqueConstraints = {
        @UniqueConstraint(
            columnNames = {"cricket_match_id", "innings_number"}
        )
    }
)
public class CricketInnings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Cricket match reference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cricket_match_id", nullable = false)
    private CricketMatch cricketMatch;

    // Innings information
    @Column(name = "innings_number", nullable = false)
    private int inningsNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batting_team_id", nullable = false)
    private TeamRegistration battingTeam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bowling_team_id", nullable = false)
    private TeamRegistration bowlingTeam;

    // Score information
    private int totalRuns = 0;
    private int totalWickets = 0;
    private int legalBalls = 0;

    // Extras
    private int totalExtras = 0;
    private int wides = 0;
    private int noBalls = 0;
    private int byes = 0;
    private int legByes = 0;

    // Target for chasing innings
    private Integer target;

    // Current batting and bowling players
    @Column(name = "striker_id")
    private Long strikerId;

    @Column(name = "non_striker_id")
    private Long nonStrikerId;

    @Column(name = "current_bowler_id")
    private Long currentBowlerId;

    @Enumerated(EnumType.STRING)
    private InningsStatus status = InningsStatus.NOT_STARTED;

    public CricketInnings() {
    }

    public enum InningsStatus {
        NOT_STARTED,
        IN_PROGRESS,
        COMPLETED
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CricketMatch getCricketMatch() {
        return cricketMatch;
    }

    public void setCricketMatch(CricketMatch cricketMatch) {
        this.cricketMatch = cricketMatch;
    }

    public int getInningsNumber() {
        return inningsNumber;
    }

    public void setInningsNumber(int inningsNumber) {
        this.inningsNumber = inningsNumber;
    }

    public TeamRegistration getBattingTeam() {
        return battingTeam;
    }

    public void setBattingTeam(TeamRegistration battingTeam) {
        this.battingTeam = battingTeam;
    }

    public TeamRegistration getBowlingTeam() {
        return bowlingTeam;
    }

    public void setBowlingTeam(TeamRegistration bowlingTeam) {
        this.bowlingTeam = bowlingTeam;
    }

    public int getTotalRuns() {
        return totalRuns;
    }

    public void setTotalRuns(int totalRuns) {
        this.totalRuns = totalRuns;
    }

    public int getTotalWickets() {
        return totalWickets;
    }

    public void setTotalWickets(int totalWickets) {
        this.totalWickets = totalWickets;
    }

    public int getLegalBalls() {
        return legalBalls;
    }

    public void setLegalBalls(int legalBalls) {
        this.legalBalls = legalBalls;
    }

    public int getTotalExtras() {
        return totalExtras;
    }

    public void setTotalExtras(int totalExtras) {
        this.totalExtras = totalExtras;
    }

    public int getWides() {
        return wides;
    }

    public void setWides(int wides) {
        this.wides = wides;
    }

    public int getNoBalls() {
        return noBalls;
    }

    public void setNoBalls(int noBalls) {
        this.noBalls = noBalls;
    }

    public int getByes() {
        return byes;
    }

    public void setByes(int byes) {
        this.byes = byes;
    }

    public int getLegByes() {
        return legByes;
    }

    public void setLegByes(int legByes) {
        this.legByes = legByes;
    }

    public Integer getTarget() {
        return target;
    }

    public void setTarget(Integer target) {
        this.target = target;
    }

    public Long getStrikerId() {
        return strikerId;
    }

    public void setStrikerId(Long strikerId) {
        this.strikerId = strikerId;
    }

    public Long getNonStrikerId() {
        return nonStrikerId;
    }

    public void setNonStrikerId(Long nonStrikerId) {
        this.nonStrikerId = nonStrikerId;
    }

    public Long getCurrentBowlerId() {
        return currentBowlerId;
    }

    public void setCurrentBowlerId(Long currentBowlerId) {
        this.currentBowlerId = currentBowlerId;
    }

    public InningsStatus getStatus() {
        return status;
    }

    public void setStatus(InningsStatus status) {
        this.status = status;
    }
}
