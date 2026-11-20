package com.champions_Call.Form.Team;


import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class TeamRegistrationDto {

    @NotBlank(message = "Team name is required")
    private String teamName;

    @NotBlank(message = "Team city is required")
    private String city;

    @NotNull(message = "You must confirm eligibility requirements")
    private Boolean eligibilityConfirmed = false; // Initialize to false by default

    private MultipartFile teamLogo;
    
    // Constructors
    public TeamRegistrationDto() {}

    public TeamRegistrationDto(String teamName, String city, Boolean eligibilityConfirmed) {
        this.teamName = teamName;
        this.city = city;
        this.eligibilityConfirmed = eligibilityConfirmed;
    }

    // Getters and Setters
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

    public Boolean getEligibilityConfirmed() {
        return eligibilityConfirmed;
    }

    public void setEligibilityConfirmed(Boolean eligibilityConfirmed) {
        this.eligibilityConfirmed = eligibilityConfirmed;
    }
    
    public MultipartFile getTeamLogo() {
        return teamLogo;
    }

    public void setTeamLogo(MultipartFile teamLogo) {
        this.teamLogo = teamLogo;
    }
}