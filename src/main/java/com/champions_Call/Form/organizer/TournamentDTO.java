package com.champions_Call.Form.organizer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import org.springframework.format.annotation.DateTimeFormat;

import com.champions_Call.Validators.ValidFile;
import com.champions_Call.model.Tournament.TournamentRule;
import com.champions_Call.model.organizer.Prize;

import jakarta.validation.constraints.*;

public class TournamentDTO {

    private Long id;

    private Integer currentStep;

  // --- Basic Details ---
   
    private String tournamentName;

   
    private String tournamentType;

   
    private String sport;

    
    private String ballType = "tennis";

    
    private String venueLocation;

    
    @FutureOrPresent(message = "Start date cannot be in the past")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;

    
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;

    // --- Registration & Entry ---
    @PositiveOrZero(message = "Entry fee cannot be negative")
    private BigDecimal entryFee;

    @Min(value = 1, message = "At least 1 team must be allowed")
    private Integer maxTeamsAllowed;

    @FutureOrPresent(message = "Registration date cannot be in the past")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate registrationStartDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate registrationDeadline;
    
    private String playerCategory;


    // --- Match Format & Rules ---
    private String tournamentStyle;

    @Min(value = 1, message = "Minimum squad size must be at least 1")
    private Integer minSquadSize;
    
    @Max(value = 50, message = "Maximum squad size cannot exceed 50")
    private Integer maxSquadSize;

    private List<TournamentRule> customRules = new ArrayList<>();
    private List<Prize> prizes = new ArrayList<>();

    // --- Branding & Publish ---
    private String bannerImageUrl;

    private String backgroundStyle;

    private String tournamentAccess;

    private String description;
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    // Identity Info 
    

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getCurrentStep() {
        return currentStep;
    }

    public void setCurrentStep(Integer currentStep) {
        this.currentStep = currentStep;
    }

    // --- Getters and Setters ---
public String getTournamentName() { return tournamentName; }
    public void setTournamentName(String tournamentName) { this.tournamentName = tournamentName; }

    public String getTournamentType() {
        return tournamentType;
    }

    public void setTournamentType(String tournamentType) {
        this.tournamentType = tournamentType;
    }

    public String getSport() { return sport; }
    public void setSport(String sport) { this.sport = sport; }


    public String getBallType() { return ballType; }
    public void setBallType(String ballType) { this.ballType = ballType; }

    
    public String getVenueLocation() { return venueLocation; }
    public void setVenueLocation(String venueLocation) { this.venueLocation = venueLocation; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public BigDecimal getEntryFee() { return entryFee; }
    public void setEntryFee(BigDecimal entryFee) { this.entryFee = entryFee; }

    public Integer getMaxTeamsAllowed() { return maxTeamsAllowed; }
    public void setMaxTeamsAllowed(Integer maxTeamsAllowed) { this.maxTeamsAllowed = maxTeamsAllowed; }

    public LocalDate getRegistrationStartDate() { return registrationStartDate; }
    public void setRegistrationStartDate(LocalDate registrationStartDate) { this.registrationStartDate = registrationStartDate; }

    public LocalDate getRegistrationDeadline() { return registrationDeadline; }
    public void setRegistrationDeadline(LocalDate registrationDeadline) { this.registrationDeadline = registrationDeadline; }

    public String getPlayerCategory() { return playerCategory; }
    public void setPlayerCategory(String playerCategory) { this.playerCategory = playerCategory; }

    public String getTournamentStyle() { return tournamentStyle; }
    public void setTournamentStyle(String tournamentStyle) { this.tournamentStyle = tournamentStyle; }

    public Integer getMinSquadSize() { return minSquadSize; }
    public void setMinSquadSize(Integer minSquadSize) { this.minSquadSize = minSquadSize; }

    public Integer getMaxSquadSize() { return maxSquadSize; }
    public void setMaxSquadSize(Integer maxSquadSize) { this.maxSquadSize = maxSquadSize; }

    public List<TournamentRule> getCustomRules() { return customRules; }
    public void setCustomRules(List<TournamentRule> customRules) { this.customRules = customRules; }

    public List<Prize> getPrizes() { return prizes; }
    public void setPrizes(List<Prize> prizes) { this.prizes = prizes; }

    public String getBannerImageUrl() { return bannerImageUrl; }
    public void setBannerImageUrl(String bannerImageUrl) { this.bannerImageUrl = bannerImageUrl; }

    public String getBackgroundStyle() {
        return backgroundStyle;
    }

    // Setter
    public void setBackgroundStyle(String backgroundStyle) {
        this.backgroundStyle = backgroundStyle;
    }

    public String getTournamentAccess() { return tournamentAccess; }
    public void setTournamentAccess(String tournamentAccess) { this.tournamentAccess = tournamentAccess; }


}
