package com.champions_Call.model.Manging_Scoring.Cricket;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "cricket_ball_events",
    uniqueConstraints = {
        @UniqueConstraint(
            columnNames = {"event_id"}
        )
    },
    indexes = {
        @Index(name = "idx_ball_innings_sequence",
               columnList = "innings_id,event_sequence")
    }
)
public class CricketBallEvent {

      @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Unique event ID received from frontend
    @Column(name = "event_id", nullable = false, unique = true)
    private String eventId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "innings_id", nullable = false)
    private CricketInnings innings;

    // Delivery information
    private int overNumber;
    private int ballNumber;

    @Column(name = "event_sequence", nullable = false)
    private long eventSequence;

    // Player information
    private Long strikerId;
    private Long nonStrikerId;
    private Long bowlerId;

    // Runs
    private int runsOffBat;
    private int completedRuns;

    // Extras
    @Enumerated(EnumType.STRING)
    private ExtraType extraType = ExtraType.NONE;

    private int extraRuns;

    // Wicket information
    private boolean wicket;

    @Enumerated(EnumType.STRING)
    private WicketType wicketType;

    private Long dismissedBatterId;
    private Long newBatterId;

    // Undo information
    private boolean undone = false;

    private LocalDateTime createdAt = LocalDateTime.now();

    public CricketBallEvent() {
    }

    public enum ExtraType {
        NONE,
        WIDE,
        NO_BALL,
        BYE,
        LEG_BYE
    }

    public enum WicketType {
        BOWLED,
        CAUGHT,
        LBW,
        RUN_OUT,
        STUMPED,
        HIT_WICKET,
        RETIRED_OUT,
        OTHER
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public CricketInnings getInnings() {
        return innings;
    }

    public void setInnings(CricketInnings innings) {
        this.innings = innings;
    }

    public int getOverNumber() {
        return overNumber;
    }

    public void setOverNumber(int overNumber) {
        this.overNumber = overNumber;
    }

    public int getBallNumber() {
        return ballNumber;
    }

    public void setBallNumber(int ballNumber) {
        this.ballNumber = ballNumber;
    }

    public long getEventSequence() {
        return eventSequence;
    }

    public void setEventSequence(long eventSequence) {
        this.eventSequence = eventSequence;
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

    public Long getBowlerId() {
        return bowlerId;
    }

    public void setBowlerId(Long bowlerId) {
        this.bowlerId = bowlerId;
    }

    public int getRunsOffBat() {
        return runsOffBat;
    }

    public void setRunsOffBat(int runsOffBat) {
        this.runsOffBat = runsOffBat;
    }

    public int getCompletedRuns() {
        return completedRuns;
    }

    public void setCompletedRuns(int completedRuns) {
        this.completedRuns = completedRuns;
    }

    public ExtraType getExtraType() {
        return extraType;
    }

    public void setExtraType(ExtraType extraType) {
        this.extraType = extraType;
    }

    public int getExtraRuns() {
        return extraRuns;
    }

    public void setExtraRuns(int extraRuns) {
        this.extraRuns = extraRuns;
    }

    public boolean isWicket() {
        return wicket;
    }

    public void setWicket(boolean wicket) {
        this.wicket = wicket;
    }

    public WicketType getWicketType() {
        return wicketType;
    }

    public void setWicketType(WicketType wicketType) {
        this.wicketType = wicketType;
    }

    public Long getDismissedBatterId() {
        return dismissedBatterId;
    }

    public void setDismissedBatterId(Long dismissedBatterId) {
        this.dismissedBatterId = dismissedBatterId;
    }

    public Long getNewBatterId() {
        return newBatterId;
    }

    public void setNewBatterId(Long newBatterId) {
        this.newBatterId = newBatterId;
    }

    public boolean isUndone() {
        return undone;
    }

    public void setUndone(boolean undone) {
        this.undone = undone;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
