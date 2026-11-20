package com.champions_Call.Form.Player;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import com.champions_Call.Validators.ValidFile;

import jakarta.validation.constraints.Pattern;


public class PlayerProfileDTO {

    
    private String fullName;
    private String username;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateOfBirth;

    private String city;

    @ValidFile
    private MultipartFile profileImage;

    private String jerseyNumber;

    @ValidFile
    private MultipartFile playerIdProof;

    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be exactly 10 digits")
    private String phoneNumber;
    
    private String primarySport;
    private String playerRole;
    private String skillLevel;
    private String preferredSide;
    private String currentStatus;
    private String bio;
    private Integer yearsOfExperience;
    private String otherStrength;
    private Integer currentStep = 1;

    private boolean isCompleteProfile = false; // Default to false

    // This captures the IDs of the selected strengths from the checkbox form
    private List<Long> strengthIds = new ArrayList<>();

    private boolean verified = false;

    private String idType;

    

    private String idNumber;

    private MultipartFile idDocument; // Or String, depending on your 
    // implementation

    public String getIdType() {
        return idType;
    }   

    public void setIdType(String idType) {
        this.idType = idType;
    }

    public MultipartFile getIdDocument() {
        return idDocument;
    }

    public void setIdDocument(MultipartFile idDocument) {
        this.idDocument = idDocument;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }
    // Add this to PlayerProfile.java
    
    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }
    
    public String getCurrentStatus() { return currentStatus; }
    public void setCurrentStatus(String currentStatus) { this.currentStatus = currentStatus; }

    // Getters
    public String getFullName() { return fullName; }
    public String getUsername() { return username; }
    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }
    public String getCity() { return city; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getPrimarySport() { return primarySport; }
    public String getPlayerRole() { return playerRole; }
    public String getSkillLevel() { return skillLevel; }
    public String getPreferredSide() { return preferredSide; }
    public String getBio() { return bio; }
    public Integer getYearsOfExperience() { return yearsOfExperience; }
    public String getOtherStrength() { return otherStrength; }
    public List<Long> getStrengthIds() { return strengthIds; }

    public boolean isCompleteProfile() {
        return isCompleteProfile;
    }

    public void setCompleteProfile(boolean isCompleteProfile) {
        this.isCompleteProfile = isCompleteProfile;
    }

    public Integer getCurrentStep() {
        return currentStep;
    }

    public String getJerseyNumber() { 
        return jerseyNumber; 
    }
    public void setJerseyNumber(String jerseyNumber) { 
        this.jerseyNumber = jerseyNumber; 
    }

    public MultipartFile getPlayerIdProof() { 
        return playerIdProof; 
    }
    public void setPlayerIdProof(MultipartFile playerIdProof) { 
        this.playerIdProof = playerIdProof; 
    }

    public void setCurrentStep(Integer currentStep) {
        this.currentStep = currentStep;
    }

    public MultipartFile getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(MultipartFile profileImage) {
        this.profileImage = profileImage;
    }
    // Setters
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setUsername(String username) { this.username = username; }
    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }
    public void setCity(String city) { this.city = city; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public void setPrimarySport(String primarySport) { this.primarySport = primarySport; }
    public void setPlayerRole(String playerRole) { this.playerRole = playerRole; }
    public void setSkillLevel(String skillLevel) { this.skillLevel = skillLevel; }
    public void setPreferredSide(String preferredSide) { this.preferredSide = preferredSide; }
    public void setBio(String bio) { this.bio = bio; }
    public void setYearsOfExperience(Integer yearsOfExperience) { this.yearsOfExperience = yearsOfExperience; }
    public void setOtherStrength(String otherStrength) { this.otherStrength = otherStrength; }
    public void setStrengthIds(List<Long> strengthIds) { this.strengthIds = strengthIds; }
}
