package com.champions_Call.model.Tournament;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.champions_Call.model.organizer.OrganizerProfile;
import com.champions_Call.model.organizer.Prize;

@Entity
@Table(name = "tournaments")
public class Tournament {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- System / Relationship Fields ---
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id", nullable = false)
    private OrganizerProfile organizer;
    
    // As discussed: Complet  or Incomplet about the setup process of tournament
    // 1. Tracks if the "Create Tournament" process is done
    @Enumerated(EnumType.STRING)
    private TournamentRegistrationStatus setupStatus; 

    // 2. Tracks the Live Lifecycle of the tournament
    @Enumerated(EnumType.STRING)
    private TournamentStatus status; 

    private Integer currentStep = 1; // Default to step 1

    // --- Step 1: Basic Details ---
    private String tournamentName;
    private String tournamentType;
    private String sport;
    private String ballType;
    private String venueLocation;
    private LocalDate startDate;
    private LocalDate endDate;

    private String description;

    // --- Step 2: Registration & Entry ---
    private BigDecimal entryFee; // BigDecimal is best for currency (₹)
    private Integer maxTeamsAllowed;
    private LocalDate registrationStartDate;
    private LocalDate registrationDeadline;
    private String playerCategory;


    private Double totalRevenue;


    private Integer registeredTeams = 0;

    // --- Step 3: Match Format & Rules ---
    private String tournamentStyle;
    private Integer minSquadSize;
    private Integer maxSquadSize;

    
    @Column(name = "fixtures_generated", nullable = false)
    private boolean fixturesGenerated = false;

    
    // One-to-Many relationship (as we discussed previously)
    @OneToMany(mappedBy = "tournament", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TournamentRule> customRules = new ArrayList<>();

    // --- Step 4: Prizes & Awards ---
    
    // Since you created the Prize model!
    @OneToMany(mappedBy = "tournament", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Prize> prizes = new ArrayList<>();

    // --- Step 5: Branding & Publish ---
    private String bannerImageUrl; 
    
    // e.g., "PUBLIC" or "PRIVATE"
    private String tournamentAccess; 

    @Column(nullable = false)
    private boolean isSchedulePublished = false;

    // Getter and Setter
    public boolean isSchedulePublished() {
        return isSchedulePublished;
    }

    public void setSchedulePublished(boolean schedulePublished) {
        isSchedulePublished = schedulePublished;
    }


    public int getRegistrationPercentage() {

        int max = (this.maxTeamsAllowed == null) ? 0 : this.maxTeamsAllowed;
        int reg = (this.registeredTeams == null) ? 0 : this.registeredTeams;
        
        if (max == 0) return 0;
        return (int) ((this.registeredTeams * 100.0) / this.maxTeamsAllowed);
    }
    public String getSportIcon() {
        if (this.sport == null) return "❓";
        
        switch (this.sport.toLowerCase()) {
            case "cricket":   return "🏏";
            case "football":  return "⚽";
            case "kabaddi":   return "🤼";
            default:          return "🏅";
        }
    }
    public String getTournamentTimeStatus() {
        if (this.startDate == null) return "Date TBD";
        
        java.time.LocalDate today = java.time.LocalDate.now();
        
        // If an endDate is set and today is past the endDate, show Completed
        if (this.endDate != null && today.isAfter(this.endDate)) {
            return "Completed on " + this.endDate.format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy"));
        }
        
        // If today is between start and end date (or same as start date)
        long daysToStart = java.time.temporal.ChronoUnit.DAYS.between(today, this.startDate);
        
        if (daysToStart > 0) {
            return daysToStart + " days remaining";
        } else if (daysToStart == 0 || (this.endDate != null && !today.isAfter(this.endDate))) {
            return "🔥 Tournament is Live / Ongoing";
        } else {
            return "Started on " + this.startDate.format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy"));
        }
    }
    public boolean isPublished() {
        return this.status != TournamentStatus.UNPUBLISHED;
    }

    public String backgroundStyle() {
        if (this.bannerImageUrl != null && !this.bannerImageUrl.isEmpty()) {
            return "background-image: url('" + this.bannerImageUrl + "')";
        }
        String sportKey = (this.sport != null) ? this.sport.toLowerCase() : "default";
        return "background-image: url('/Images/sport-" + sportKey + ".png')";
    }

    public boolean isFixturesGenerated() {
        return fixturesGenerated;
    }

    public void setFixturesGenerated(boolean fixturesGenerated) {
        this.fixturesGenerated = fixturesGenerated;
    }
    // ---------------- GETTERS & SETTERS ----------------

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public OrganizerProfile getOrganizer() { return organizer; }
    public void setOrganizer(OrganizerProfile organizer) { this.organizer = organizer; }

    public TournamentRegistrationStatus getSetupStatus() {
    return setupStatus;
    }

    public void setSetupStatus(TournamentRegistrationStatus setupStatus) {
        this.setupStatus = setupStatus;
    }

    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public Integer getCurrentStep() {
        return currentStep;
    }

    // Setter
    public void setCurrentStep(Integer currentStep) {
        if (currentStep >= 1 && currentStep <= 5) {
            this.currentStep = currentStep;
        } else {
            this.currentStep = 1; // Default to step 1 if an invalid step is provided
        }
    }

    public TournamentStatus getStatus() {
        return status;
    }

    public void setStatus(TournamentStatus status) {
        this.status = status;
    }

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

    public Double getTotalRevenue() {
        return (this.totalRevenue == null) ? 0.0 : this.totalRevenue;
    }

    // 3. Add this Setter (Needed if you update revenue)
    public void setTotalRevenue(Double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

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

    public String getBannerImageUrl() {
        if (bannerImageUrl != null 
        && !bannerImageUrl.isBlank() 
        && !bannerImageUrl.contains("sport-")) {   // reject legacy bad values
        return bannerImageUrl;
    }
        return switch (sport) {
            case "CRICKET" -> "/Images/cricket-2.png";
            case "FOOTBALL" -> "/Images/football-2.png";
            case "KABADDI" -> "/Images/kabaddi-2.png";
            default -> "/Images/default-tournament-banner.png";
        }; 
    }
    public void setBannerImageUrl(String bannerImageUrl) { this.bannerImageUrl = bannerImageUrl; }

    public String getTournamentAccess() { return tournamentAccess; }
    public void setTournamentAccess(String tournamentAccess) { this.tournamentAccess = tournamentAccess; }

    public int getRegisteredTeams() {
        return registeredTeams;
    }

    public void setRegisteredTeams(int registeredTeams) {
        this.registeredTeams = registeredTeams;
    }

}