package com.champions_Call.model.player;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import com.champions_Call.Helper.PlayerStatus;
import com.champions_Call.Validators.ValidFile;
import com.champions_Call.model.User;

@Entity
@Table(name = "player_profiles")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlayerProfile {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name="user_id",referencedColumnName="id",unique=true,nullable=false)
    private User user;

    private String ProfileImage;
    private String fullName;
    private String username;


    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateOfBirth;

    private String city;
    private String phoneNumber;
    private String primarySport;
    private String playerRole;
    private String skillLevel;
    private String preferredSide;

    private Integer currentStep = 1;

    // Add this to PlayerProfile.java
    private String currentStatus;

    // Generate getters and setters for it
    public String getCurrentStatus() { return currentStatus; }
    public void setCurrentStatus(String currentStatus) { this.currentStatus = currentStatus; }
    
    @Column(columnDefinition = "TEXT")
    private String bio;
    
    private Integer yearsOfExperience;
    
    // This allows players to add custom strengths not in the predefined list
    private String otherStrength;

    private boolean isCompleteProfile = false; // Default to false

    @Column(name = "cloudinary_image_public_id")
    private String cloudinaryImagePublicId;

    private String jerseyNumber;

    private String playerIdProof;

    private int totalMatches = 0;       
    private int wins = 0;               
    private int losses = 0;             
    private double averagePerformance = 0.0;


    private boolean verified = false;

    @Enumerated(EnumType.STRING)    
    private PlayerStatus playerStatus = PlayerStatus.AVAILABLE;

    private String idType;

    public String getIdType() {
        return idType;
    }

    public void setIdType(String idType) {
        this.idType = idType;
    }

    // Getters and Setters
    public PlayerStatus getPlayerStatus() {
        return playerStatus;
    }

    public void setPlayerStatus(PlayerStatus playerStatus) {
        this.playerStatus = playerStatus;
    }


    private String idNumber;

    public String getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }
    // Many-to-Many relationship with Strength entity
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "player_strengths",
        joinColumns = @JoinColumn(name = "player_profile_id"),
        inverseJoinColumns = @JoinColumn(name = "strength_id")
    )
    private Set<Strength> strengths = new HashSet<>();


     public String getJerseyNumber() { 
        return jerseyNumber; 
    }
    public void setJerseyNumber(String jerseyNumber) { 
        this.jerseyNumber = jerseyNumber; 
    }

    public String getPlayerIdProof() { 
        return playerIdProof; 
    }
    public void setPlayerIdProof(String playerIdProof) { 
        this.playerIdProof = playerIdProof; 
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public Integer getCurrentStep() {
        return currentStep;
    }
    public boolean isCompleteProfile() {
        return isCompleteProfile;
    }

    public void setCompleteProfile(boolean isCompleteProfile) {
        this.isCompleteProfile = isCompleteProfile;
    }

    public void setCurrentStep(Integer currentStep) {
        this.currentStep = currentStep;
    }

    public String getProfileImage() {
        return ProfileImage;
    }

    public void setProfileImage(String ProfileImage) {
        this.ProfileImage = ProfileImage;
    }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getPrimarySport() { return primarySport; }
    public void setPrimarySport(String primarySport) { this.primarySport = primarySport; }

    public String getPlayerRole() { return playerRole; }
    public void setPlayerRole(String playerRole) { this.playerRole = playerRole; }

    public String getSkillLevel() { return skillLevel; }
    public void setSkillLevel(String skillLevel) { this.skillLevel = skillLevel; }

    public String getPreferredSide() { return preferredSide; }
    public void setPreferredSide(String preferredSide) { this.preferredSide = preferredSide; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public Integer getYearsOfExperience() { return yearsOfExperience; }
    public void setYearsOfExperience(Integer yearsOfExperience) { this.yearsOfExperience = yearsOfExperience; }



    public String getOtherStrength() { return otherStrength; }
    public void setOtherStrength(String otherStrength) { this.otherStrength = otherStrength; }

    public Set<Strength> getStrengths() { return strengths; }
    public void setStrengths(Set<Strength> strengths) { this.strengths = strengths; }

    public String getCloudinaryImagePublicId() {
        return cloudinaryImagePublicId;
    }

    public void setCloudinaryImagePublicId(String cloudinaryImagePublicId) {
        this.cloudinaryImagePublicId = cloudinaryImagePublicId;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    // ==========================================
    // GETTERS
    // ==========================================
    public int getTotalMatches() {
        return totalMatches;
    }

    public int getWins() {
        return wins;
    }

    public int getLosses() {
        return losses;
    }

    public double getAveragePerformance() {
        return averagePerformance;
    }

    // ==========================================
    // SETTERS
    // ==========================================
    public void setTotalMatches(int totalMatches) {
        this.totalMatches = totalMatches;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }

    public void setLosses(int losses) {
        this.losses = losses;
    }

    public void setAveragePerformance(double averagePerformance) {
        this.averagePerformance = averagePerformance;
    }
    
}
