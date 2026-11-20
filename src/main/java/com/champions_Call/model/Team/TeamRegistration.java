package com.champions_Call.model.Team;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.champions_Call.Helper.RegistrationTeamStatus;
import com.champions_Call.Helper.TournamentOutcome;
import com.champions_Call.model.User;
import com.champions_Call.model.Tournament.Tournament;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "team_registrations", indexes = {
    @Index(name = "idx_tournament_team", columnList = "tournament_id, teamName", unique = true)
})
public class TeamRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id", nullable = false)
    private Tournament tournament;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User registeredBy;

    @Column(nullable = false)
    private String teamName;
    
    @Column(nullable = false)
    private String city;

    // Made nullable so it doesn't fail basic form submission before roster selection
    @Column(nullable = true)    
    private Integer squadSize;

    // Unique invite code/token for shareable links
    @Column(unique = true, nullable = false, length = 10)
    private String inviteCode = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

    private String captainName;
    private String email;
    private String phone;
    
    private String primaryJerseyColor;
    private String secondaryJerseyColor;
    
    private Double entryFeePaid;
    private String paymentReferenceId;


    private String teamLogo;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RegistrationTeamStatus status = RegistrationTeamStatus.PENDING; 

    private String rejectionReason;

    @JsonManagedReference
    @OneToMany(mappedBy = "teamRegistration", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TeamRoster> roster = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    private TournamentOutcome tournamentOutcome;

    // Getters and Setters
    public TournamentOutcome getTournamentOutcome() {
        return tournamentOutcome;
    }

    public void setTournamentOutcome(TournamentOutcome tournamentOutcome) {
        this.tournamentOutcome = tournamentOutcome;
    }

    @PreUpdate
    public void setLastUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Constructors
    public TeamRegistration() {}

    // Helper method to generate the full shareable invite URL
    public String getShareableInviteLink() {
        return "http://localhost:8080/api/organizer/tournament/join-team/" + this.inviteCode;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Tournament getTournament() {
        return tournament;
    }

    public void setTournament(Tournament tournament) {
        this.tournament = tournament;
    }

    public User getRegisteredBy() {
        return registeredBy;
    }

    public void setRegisteredBy(User registeredBy) {
        this.registeredBy = registeredBy;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Integer getSquadSize() {
        return squadSize;
    }

    public void setSquadSize(Integer squadSize) {
        this.squadSize = squadSize;
    }

    public String getInviteCode() {
        return inviteCode;
    }

    public void setInviteCode(String inviteCode) {
        this.inviteCode = inviteCode;
    }

    public String getCaptainName() {
        return captainName;
    }

    public void setCaptainName(String captainName) {
        this.captainName = captainName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPrimaryJerseyColor() {
        return primaryJerseyColor;
    }

    public void setPrimaryJerseyColor(String primaryJerseyColor) {
        this.primaryJerseyColor = primaryJerseyColor;
    }

    public String getSecondaryJerseyColor() {
        return secondaryJerseyColor;
    }

    public void setSecondaryJerseyColor(String secondaryJerseyColor) {
        this.secondaryJerseyColor = secondaryJerseyColor;
    }

    public Double getEntryFeePaid() {
        return entryFeePaid;
    }

    public void setEntryFeePaid(Double entryFeePaid) {
        this.entryFeePaid = entryFeePaid;
    }

    public String getPaymentReferenceId() {
        return paymentReferenceId;
    }

    public void setPaymentReferenceId(String paymentReferenceId) {
        this.paymentReferenceId = paymentReferenceId;
    }

    public RegistrationTeamStatus getStatus() {
        return status;
    }

    public void setStatus(RegistrationTeamStatus status) {
        this.status = status;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public List<TeamRoster> getRoster() {
        return roster;
    }

    public void setRoster(List<TeamRoster> roster) {
        this.roster = roster;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public String getTeamLogo() {
        return teamLogo;
    }

    public void setTeamLogo(String teamLogo) {
        this.teamLogo = teamLogo;
    }

    // Dynamic initials helper (no database column needed!)
public String getTeamInitials() {
    if (this.teamName == null || this.teamName.trim().isEmpty()) {
        return "TM";
    }
    String[] words = this.teamName.trim().split("\\s+");
    if (words.length >= 2) {
        return ("" + words[0].charAt(0) + words[1].charAt(0)).toUpperCase();
    } else if (words[0].length() >= 2) {
        return words[0].substring(0, 2).toUpperCase();
    }
    return words[0].substring(0, 1).toUpperCase();
}
}