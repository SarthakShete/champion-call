
package com.champions_Call.model.Manging_Scoring.Cricket;

import com.champions_Call.model.player.PlayerProfile;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
    name = "cricket_player_stats",
    uniqueConstraints = {
        @UniqueConstraint(
            columnNames = {"innings_id", "player_id"}
        )
    }
)
public class CricketPlayerStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "innings_id", nullable = false)
    private CricketInnings innings;

    // Player reference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private PlayerProfile player;

    // Batting statistics
    private int runs = 0;
    private int ballsFaced = 0;
    private int fours = 0;
    private int sixes = 0;

    private boolean dismissed = false;
    private String dismissalType;

    // Bowling statistics
    private int legalBallsBowled = 0;
    private int runsConceded = 0;
    private int wickets = 0;
    private int maidens = 0;

    public CricketPlayerStats() {
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CricketInnings getInnings() {
        return innings;
    }

    public void setInnings(CricketInnings innings) {
        this.innings = innings;
    }

    public PlayerProfile getPlayer() {
        return player;
    }

    public void setPlayer(PlayerProfile player) {
        this.player = player;
    }

    public int getRuns() {
        return runs;
    }

    public void setRuns(int runs) {
        this.runs = runs;
    }

    public int getBallsFaced() {
        return ballsFaced;
    }

    public void setBallsFaced(int ballsFaced) {
        this.ballsFaced = ballsFaced;
    }

    public int getFours() {
        return fours;
    }

    public void setFours(int fours) {
        this.fours = fours;
    }

    public int getSixes() {
        return sixes;
    }

    public void setSixes(int sixes) {
        this.sixes = sixes;
    }

    public boolean isDismissed() {
        return dismissed;
    }

    public void setDismissed(boolean dismissed) {
        this.dismissed = dismissed;
    }

    public String getDismissalType() {
        return dismissalType;
    }

    public void setDismissalType(String dismissalType) {
        this.dismissalType = dismissalType;
    }

    public int getLegalBallsBowled() {
        return legalBallsBowled;
    }

    public void setLegalBallsBowled(int legalBallsBowled) {
        this.legalBallsBowled = legalBallsBowled;
    }

    public int getRunsConceded() {
        return runsConceded;
    }

    public void setRunsConceded(int runsConceded) {
        this.runsConceded = runsConceded;
    }

    public int getWickets() {
        return wickets;
    }

    public void setWickets(int wickets) {
        this.wickets = wickets;
    }

    public int getMaidens() {
        return maidens;
    }

    public void setMaidens(int maidens) {
        this.maidens = maidens;
    }
}
