package com.champions_Call.model.Team;

import com.champions_Call.Helper.RosterRole;
import com.champions_Call.Helper.TournamentRole;
import com.champions_Call.model.player.PlayerProfile;
import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.*;

@Entity
@Table(name = "team_roster")
public class TeamRoster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Links the player to the team registration
    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_registration_id", nullable = false)
    private TeamRegistration teamRegistration;

    // Pulls core identity directly from your existing Player/User profile
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private PlayerProfile playerProfile; 

    // Tournament-specific squad attributes
    private String jerseyNumber;
    
    @Enumerated(EnumType.STRING)
    private TournamentRole roleInTeam; // e.g., CAPTAIN, VICE_CAPTAIN, WK, PLAYER

    // Constructors
    public TeamRoster() {}

    public TeamRoster(TeamRegistration teamRegistration, PlayerProfile playerProfile, String jerseyNumber, TournamentRole roleInTeam) {
        this.teamRegistration = teamRegistration;
        this.playerProfile = playerProfile;
        this.jerseyNumber = jerseyNumber;
        this.roleInTeam = roleInTeam;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TeamRegistration getTeamRegistration() {
        return teamRegistration;
    }

    public void setTeamRegistration(TeamRegistration teamRegistration) {
        this.teamRegistration = teamRegistration;
    }

    public PlayerProfile getPlayerProfile() {
        return playerProfile;
    }

    public void setPlayerProfile(PlayerProfile playerProfile) {
        this.playerProfile = playerProfile;
    }

    public String getJerseyNumber() {
        return jerseyNumber;
    }

    public void setJerseyNumber(String jerseyNumber) {
        this.jerseyNumber = jerseyNumber;
    }

    public TournamentRole getRoleInTeam() {
        return roleInTeam;
    }

     public void setRoleInTeam(TournamentRole player) {
        this.roleInTeam = player;
    }
}
