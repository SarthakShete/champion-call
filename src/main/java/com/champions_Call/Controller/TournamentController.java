package com.champions_Call.Controller;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.champions_Call.Form.Match.ScheduleDayDTO;
import com.champions_Call.Form.Match.ScoringAccessDTO;
import com.champions_Call.Form.Player.PlayerDetailDTO;
import com.champions_Call.Form.Team.RosterEntryDTO;
import com.champions_Call.Form.Team.TeamRegistrationDto;
import com.champions_Call.Form.organizer.NotificationDTO;
import com.champions_Call.Form.organizer.TournamentDTO;
import com.champions_Call.Helper.AppConstants;
import com.champions_Call.Helper.MatchStatus;
import com.champions_Call.Helper.RegistrationTeamStatus;
import com.champions_Call.Helper.RosterRole;
import com.champions_Call.Helper.TournamentOutcome;
import com.champions_Call.Helper.TournamentRole;
import com.champions_Call.mapper.TournamentMapper;
import com.champions_Call.model.User;
import com.champions_Call.model.Team.TeamRegistration;
import com.champions_Call.model.Team.TeamRoster;
import com.champions_Call.model.Tournament.Tournament;
import com.champions_Call.model.Tournament.TournamentRegistrationStatus;
import com.champions_Call.model.Tournament.TournamentRule;
import com.champions_Call.model.Tournament.TournamentStatus;
import com.champions_Call.model.match.ManualFixturesRequestDto;
import com.champions_Call.model.match.ManualFixturesRequestDto.ManualFixtureItemDto;
import com.champions_Call.model.match.Match;
import com.champions_Call.model.organizer.OrganizerProfile;
import com.champions_Call.model.organizer.Prize;
import com.champions_Call.model.player.PlayerProfile;
import com.champions_Call.repository.TournamentRepository;
import com.champions_Call.service.FileUploadService;
import com.champions_Call.service.ImageService;
import com.champions_Call.service.OrganizerService;
import com.champions_Call.service.PlayerService;
import com.champions_Call.service.SecurityUtils;
import com.champions_Call.service.TeamRegistrationService;
import com.champions_Call.service.TeamRosterService;
import com.champions_Call.service.TournamentService;
import com.champions_Call.service.EmailNotificationService;

import com.fasterxml.jackson.core.JsonProcessingException;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;


import com.champions_Call.service.MatchService;
import com.champions_Call.service.NotificationService;


@Controller
@RequestMapping("/api/organizer/tournament")
public class TournamentController {

  
  @Autowired
  private OrganizerService organizerService;

  @Autowired
  private TournamentService tournamentService;

  @Autowired
  private TournamentRepository tournamentRepo;

  @Autowired
  private FileUploadService fileUploadService;

  @Autowired
  private MatchService matchService;

  @Autowired
  private SecurityUtils securityUtils;

  @Autowired
  private TournamentMapper tournamentMapper;

  @Autowired
  private ImageService imageService;

  @Autowired 
  private PlayerService playerService;

  @Autowired
  private TeamRosterService teamRosterService;

  @Autowired
  private TeamRegistrationService teamRegistrationService;

  @Autowired
  private EmailNotificationService emailNotificationService;


  @Autowired
  private NotificationService notificationService;

    @GetMapping("/list")
    public String TournamentList(Model model,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = AppConstants.PAGE_SIZE) int size
    ){
        Pageable pageable = PageRequest.of(page, size, Sort.by("startDate").ascending());

        // Create a specification to exclude UNPUBLISHED tournaments for public viewing
        Specification<Tournament> spec = (root, query, cb) -> {
            return cb.notEqual(root.get("status"), TournamentStatus.UNPUBLISHED);
        };

        // Fetch paginated tournaments using the specification
        Page<Tournament> tournamentPage = tournamentService.findAll(spec, pageable);

        // Take a logged-in user    
        User user = securityUtils.getLoggedInUser();

        PlayerProfile playerProfile = null;

        if (user != null) {
            playerProfile = playerService.getPlayerProfileByUser(user).orElse(null);
        }

        // Only add profile if it's not null, or pass null safely without calling methods on it
        if (playerProfile != null) {
            model.addAttribute("profile", playerProfile);
        }
        model.addAttribute("tournaments", tournamentPage.getContent());
        model.addAttribute("currentPage", tournamentPage.getNumber());
        model.addAttribute("totalPages", tournamentPage.getTotalPages());
        model.addAttribute("hasNext", tournamentPage.hasNext());
        model.addAttribute("hasPrevious", tournamentPage.hasPrevious());

        return "tournament/tournamentList";
    }

    @GetMapping("/tournament-details/{id}")
    public String tournamentDetails(@PathVariable("id") Long id,Model model){

        Tournament tournament = tournamentService.getTournamentId(id);

        if (tournament == null) {
            System.out.println("Tournament not found "+id);
            return "tournament/tournament-details";
        }

        User user = securityUtils.getLoggedInUser();

        PlayerProfile playerProfile = playerService.getPlayerProfileByUser(user).orElse(null);

        // Fetch all registered team for particular tournament
        List<TeamRegistration> registeredTeam = teamRegistrationService.getTeamsByTournament(tournament);

        // --- DEBUG LOOP TO PRINT REGISTERED TEAMS TO CONSOLE ---
        System.out.println("----------------------------------------");
        System.out.println("DEBUG: Tournament Name -> " + tournament.getTournamentName());
        System.out.println("DEBUG: Tournament ID -> " + tournament.getId());
        System.out.println("DEBUG: Total Registered Teams Found -> " + registeredTeam.size());

        for(TeamRegistration team : registeredTeam){
            System.out.println(" > Team ID: " + team.getId() + 
                               ", Team Name: " + team.getTeamName() + 
                               ", City: " + team.getCity() + 
                               ", Logo: " + team.getTeamLogo());
        }   

        model.addAttribute("registeredTeams",registeredTeam);


        // team Registration Model

        model.addAttribute("profile",playerProfile);
        
        model.addAttribute("tournament",tournament);

        model.addAttribute("teamRegistrationDto", new TeamRegistrationDto());


        // To show the Match Schedule here
        List<Match> matches = matchService.getTeamsByTournamentId(tournament.getId());

        // sort the matches in ascending order depending upon the date..
        matches.sort((m1, m2) -> {
            if (m1.getMatchDate() == null && m2.getMatchDate() == null) return 0;
            if (m1.getMatchDate() == null) return 1;
            if (m2.getMatchDate() == null) return -1;
            return m1.getMatchDate().compareTo(m2.getMatchDate());
        });

        // THis is for count the total matches
        int totalMatches = matches.size();

        long upcomingMatches = matches.stream()
            .filter(m -> m.getStatus() != null && ("UPCOMING".equalsIgnoreCase(m.getStatus().name()) || "SCHEDULED".equalsIgnoreCase(m.getStatus().name())))
            .count();

        long liveMatches = matches.stream()
            .filter(m -> m.getStatus() != null && "LIVE".equalsIgnoreCase(m.getStatus().name()))
            .count();

        long completedMatches = matches.stream()
            .filter(m -> m.getStatus() != null && "COMPLETED".equalsIgnoreCase(m.getStatus().name()))
            .count();

        
        model.addAttribute("totalMatches", totalMatches);
        model.addAttribute("upcomingMatches", upcomingMatches);
        model.addAttribute("liveMatches", liveMatches);
        model.addAttribute("completedMatches", completedMatches);

        // Filter matches based on status

        List<Match> upcomingMatchList = matches.stream()
            .filter(m -> m.getStatus() != null &&
                ("UPCOMING".equalsIgnoreCase(m.getStatus().name()) ||
                "SCHEDULED".equalsIgnoreCase(m.getStatus().name())))
            .collect(Collectors.toList());

        List<Match> liveMatchList = matches.stream()
            .filter(m -> m.getStatus() != null &&
                "LIVE".equalsIgnoreCase(m.getStatus().name()))
            .collect(Collectors.toList());

        List<Match> completedMatchList = matches.stream()
            .filter(m -> m.getStatus() != null &&
                "COMPLETED".equalsIgnoreCase(m.getStatus().name()))
            .collect(Collectors.toList());

        // Send filtered lists to Thymeleaf

        model.addAttribute("upcomingMatchList", upcomingMatchList);
        model.addAttribute("liveMatchList", liveMatchList);
        model.addAttribute("completedMatchList", completedMatchList);

        // Group matches by their matchDate using a LinkedHashMap to preserve the ascending order
        Map<LocalDate, List<Match>> matchesByDate = matches.stream()
            .collect(Collectors.groupingBy(
                Match::getMatchDate, 
                java.util.LinkedHashMap::new, 
                Collectors.toList()
            ));

        // Convert the map into your ScheduleDayDTO list for Thymeleaf
        List<ScheduleDayDTO> scheduleDays = matchesByDate.entrySet().stream()
            .map(entry -> new ScheduleDayDTO(entry.getKey(), entry.getValue()))
            .collect(Collectors.toList());

        // Add the grouped schedule list to the model
        model.addAttribute("scheduleDays", scheduleDays);

        

        return "tournament/tournament-details";
    }

    // helper method goes here, after the method above and before the class's last brace
    private List<ScheduleDayDTO> groupByDate(List<Match> list) {
        return list.stream()
            .filter(m -> m.getMatchDate() != null)
            .collect(Collectors.groupingBy(Match::getMatchDate,
                     java.util.LinkedHashMap::new, Collectors.toList()))
            .entrySet().stream()
            .map(e -> new ScheduleDayDTO(e.getKey(), e.getValue()))
            .collect(Collectors.toList());
    }

    // 2. PROCESS TEAM REGISTRATION FORM SUBMISSION
    @PostMapping("/{id}/register")
    public String registerTeam(
            @PathVariable("id") Long id,
            @Valid @ModelAttribute("teamRegistrationDto") TeamRegistrationDto dto,
            BindingResult result,
            @RequestParam(value = "teamLogo", required = false) MultipartFile file,
            Model model,
            RedirectAttributes redirectAttributes
    ) {

       
        
        Tournament tournament = tournamentService.getTournamentId(id);
        User user = securityUtils.getLoggedInUser();

        System.out.println("Team Registration is called....");

        System.out.println("THe team name is : "+dto.getTeamName());

        if (user == null) {
            return "redirect:/login";
        }

        // To check if player is already register for particular or not...
        boolean alreadyRegistered = teamRegistrationService.hasUserRegisteredForTournament(tournament,user);

        if(alreadyRegistered){
            System.out.println("User is duplicate");
            redirectAttributes.addFlashAttribute("errorMessage", "You have already registered a team for this tournament!");
            //return "redirect:/api/organizer/tournament/tournament-details/" + id;

            return "redirect:/api/organizer/tournament/tournament-details/" + id;
        }

        // If form validation fails, re-render the view with errors
       // If form validation fails, re-render the view with errors
        if (result.hasErrors() || tournament == null) {
            System.out.println("VALIDATION FAILED ERRORS: ");
            
            // Check if the error is specifically about eligibility / rules confirmation
            boolean onlyEligibilityError = result.getFieldErrors().stream()
                    .allMatch(error -> error.getField().equals("eligibilityConfirmed"));

            PlayerProfile playerProfile = playerService.getPlayerProfileByUser(user).orElse(null);
            model.addAttribute("profile", playerProfile);
            model.addAttribute("tournament", tournament);

            // If it's specifically a rule violation, pass a flag to open the rules tab
            if (onlyEligibilityError) {
                model.addAttribute("activeTab", "rules");
            } else {
                model.addAttribute("activeTab", "registration");
            }

            return "tournament/tournament-details";
        }

        // Fetch logged-in captain's player profile details
        PlayerProfile playerProfile = playerService.getPlayerProfileByUser(user).orElse(null);

        TeamRegistration registration = new TeamRegistration();

        if (file != null && !file.isEmpty()) {
            String fileName = "team_" + dto.getTeamName().replaceAll("\\s+", "_") + "_" + UUID.randomUUID();
            String url = imageService.uploadImage(file, fileName);
            if (url != null) {
                registration.setTeamLogo(url);
            }
        }

        // Automatically attach captain contact info from PlayerProfile / User
        if (playerProfile != null) {
            registration.setCaptainName(playerProfile.getFullName());
            registration.setPhone(playerProfile.getPhoneNumber());
        }
        registration.setEmail(user.getEmail());

        // Map DTO to Entity
       
        registration.setTournament(tournament);
        registration.setTeamName(dto.getTeamName());
        registration.setCity(dto.getCity());
        registration.setRegisteredBy(user);

        registration.setTournamentOutcome(TournamentOutcome.PARTICIPATED);

        if (tournament.getMaxSquadSize() != null) {
            // Default to 0 or tournament's minimum squad requirement if available
            registration.setSquadSize(tournament.getMaxSquadSize()); 
        }
        else registration.setSquadSize(0);

        

        // Save to Database
       TeamRegistration savedTeam = teamRegistrationService.saveTeam(registration);

        if(playerProfile != null){
            // create a object TeamRoster....
            TeamRoster captainRoster = new TeamRoster();
            captainRoster.setTeamRegistration(savedTeam);
            captainRoster.setPlayerProfile(playerProfile);
            captainRoster.setRoleInTeam(TournamentRole.CAPTAIN);

            teamRosterService.saveTeamRoster(captainRoster);
        }

        redirectAttributes.addFlashAttribute("successPopup", "Team registered successfully! Share your invite link with your players.");
        redirectAttributes.addFlashAttribute("registeredTournamentName", tournament.getTournamentName());

        redirectAttributes.addFlashAttribute("successMessage", "Team registered successfully! Share your invite link with your players.");

        return "redirect:/api/player/profile";
    }

    @GetMapping("/new")
    public String showCreateForm(HttpServletRequest req,HttpSession session,Model model) {

       User user = securityUtils.getLoggedInUser();

       if(user==null){
          // No user found, send user to login page
          return "redirect:/login";
       }

      OrganizerProfile organizerProfile = organizerService.getByUser(user);
      model.addAttribute("organizerProfile",organizerProfile);

      session.removeAttribute("tournamentDTO");

      // creating the empty object of tournament DTP
      TournamentDTO tournamentDTO = new TournamentDTO();
      tournamentDTO.getCustomRules().add(new TournamentRule());
      model.addAttribute("tournamentDTO",tournamentDTO);
      return "organizer/createTournament";
    } 


    @PostMapping("/save-draft")
    public String saveDraft(
      @Valid @ModelAttribute("tournamentDTO") TournamentDTO tournamentDTO,
      BindingResult result,
      HttpServletRequest req
     ){
           System.out.println("save-Draft calling...");
        if(result.hasErrors()){
          return "organizer/createTournament";
        }

        User user = securityUtils.getLoggedInUser();

       if(user==null){
          // No user found, send user to login page
          return "redirect:/login";
       }

        OrganizerProfile organizerProfile = organizerService.getByUser(user);

        return "organizer/Dashboard";
    } 
    
    
    @PostMapping("/save-step")
    public ResponseEntity<?> saveStep(@Valid @ModelAttribute TournamentDTO dto,
      BindingResult result,
      @RequestParam(value = "bannerImage", required = false) MultipartFile file,
      HttpServletRequest request,
      
      Model model
    ){
      Tournament tournament;

      System.out.println("DEBUG: Incoming DTO ID is: " + dto.getId());
      System.out.println("The Tournament Description : " + dto.getDescription());

      if (result.hasErrors()) {
       return ResponseEntity.badRequest().body(Map.of(
            "success", false, 
            "message", result.getFieldError().getDefaultMessage()
        ));
      }

      
      boolean isDuplicate;
      if (dto.getId() != null) {
        isDuplicate = tournamentService.isTournamentNameExistAndIdNot(
            dto.getTournamentName(), 
            dto.getId()
        );
      } else {
          // Only if it's a brand new tournament
          isDuplicate = tournamentService.isTournamentNameExist(dto.getTournamentName());
      }

      if (isDuplicate) {
          return ResponseEntity.badRequest().body(Map.of(
            "success", false, 
            "message", "This tournament name is already registered! Please use another one."
        ));
      }

       System.out.println("save step is called....");
        // 1. Handle file upload if present
        if (file != null && !file.isEmpty()) {
            String url = fileUploadService.save(file);
            dto.setBannerImageUrl(url);
        }
       
       if(dto.getId() != null){
         // upadate the existing one...

          tournament = tournamentService.getTournamentId(dto.getId());
       }
       else{
          // Not tournament added into db so create new Tournament
          // 1. Get the current logged-in user
          User user = securityUtils.getLoggedInUser();

          // 2. Fetch the organizer profile
          OrganizerProfile organizer = organizerService.getByUser(user);

          tournament = new Tournament();

          // Initialize the Setup as Incomplete (because they are just starting)
          tournament.setSetupStatus(TournamentRegistrationStatus.INCOMPLETE);

          // Initialize the Lifecycle as Unpublished (because it's not live yet)
          tournament.setStatus(TournamentStatus.UNPUBLISHED);
          tournament.setOrganizer(organizer);
       }

       // now paste the values of dto into the tournament object

       if(dto.getTournamentName() != null && !dto.getTournamentName().isEmpty()){
          System.out.println("The Tournament name is : "+dto.getTournamentName());
         tournament.setTournamentName(dto.getTournamentName());
       }
       if(dto.getTournamentType() != null && !dto.getTournamentType().isEmpty()){
         System.out.println("The Tournament type is : "+dto.getTournamentType());
        tournament.setTournamentType(dto.getTournamentType());
       }
       if(dto.getSport() != null && !dto.getSport().isEmpty()){
        tournament.setSport(dto.getSport());
       }
       if (dto.getBallType() != null && !dto.getBallType().isEmpty()) {
            tournament.setBallType(dto.getBallType());
        }

        if(dto.getDescription()!=null && !dto.getDescription().isEmpty()){
            tournament.setDescription(dto.getDescription());
        }
       if(dto.getVenueLocation() != null && !dto.getVenueLocation().isEmpty()){
        tournament.setVenueLocation(dto.getVenueLocation());
       }
       if(dto.getStartDate() != null){
        tournament.setStartDate(dto.getStartDate());
       }
       if(dto.getEndDate() != null){
        tournament.setEndDate(dto.getEndDate());
       }
       if(dto.getEntryFee() != null){
        tournament.setEntryFee(dto.getEntryFee());
       }
       if(dto.getMaxTeamsAllowed() != null){
        tournament.setMaxTeamsAllowed(dto.getMaxTeamsAllowed());
       }
       if(dto.getRegistrationStartDate() != null){
        tournament.setRegistrationStartDate(dto.getRegistrationStartDate());
       }
       if(dto.getRegistrationDeadline() != null){
        tournament.setRegistrationDeadline(dto.getRegistrationDeadline());
       }
       if(dto.getPlayerCategory() != null && !dto.getPlayerCategory().isEmpty()){
         tournament.setPlayerCategory(dto.getPlayerCategory());
       }
       if(dto.getTournamentStyle() != null && !dto.getTournamentStyle().isEmpty()){
        tournament.setTournamentStyle(dto.getTournamentStyle());
       }
       if(dto.getMinSquadSize() != null){
        tournament.setMinSquadSize(dto.getMinSquadSize());
       }
       if(dto.getMaxSquadSize() != null){
        tournament.setMaxSquadSize(dto.getMaxSquadSize());
       }

       // 5. Update File
        String fileName = "tournament_" + tournament.getTournamentName().replaceAll("\\s+", "_") + "_" + UUID.randomUUID();
        String url = imageService.uploadImage(file, fileName);
        if (url != null) {
            tournament.setBannerImageUrl(url);
        }
         
        if (dto.getCustomRules() != null) {
            List<TournamentRule> incomingRules = dto.getCustomRules().stream()
                .filter(r -> r != null &&
                        ((r.getDescription() != null && !r.getDescription().trim().isEmpty()) ||
                        (r.getRuleText() != null && !r.getRuleText().trim().isEmpty())))
                .collect(Collectors.toList());

            // Only replace the persisted rules if THIS step actually submitted rule data.
            // Steps that don't include the rules form must never wipe previously saved rules.
            if (!incomingRules.isEmpty()) {
                tournament.getCustomRules().clear();
                for (TournamentRule dtoRule : incomingRules) {
                    String ruleContent = dtoRule.getDescription() != null
                            ? dtoRule.getDescription() : dtoRule.getRuleText();

                    TournamentRule rule = new TournamentRule();
                    rule.setDescription(ruleContent.trim());
                    rule.setRuleText(ruleContent.trim());
                    rule.setTournament(tournament);
                    tournament.getCustomRules().add(rule);
                }
            }
        }
        if (dto.getPrizes() != null) {
            List<Prize> incomingPrizes = dto.getPrizes().stream()
                .filter(p -> p != null && p.getPrizeName() != null && !p.getPrizeName().trim().isEmpty())
                .collect(Collectors.toList());

            if (!incomingPrizes.isEmpty()) {
                tournament.getPrizes().clear();
                for (Prize dtoPrize : incomingPrizes) {
                    Prize prize = new Prize();
                    prize.setPrizeName(dtoPrize.getPrizeName());
                    prize.setPrizeType(dtoPrize.getPrizeType());
                    prize.setPrizeDetails(dtoPrize.getPrizeDetails());
                    prize.setTournament(tournament);
                    tournament.getPrizes().add(prize);
                }
            }
        }
        if(dto.getTournamentAccess() != null){
          tournament.setTournamentAccess(dto.getTournamentAccess());
        }

        if (dto.getCurrentStep() != null) {
            int nextStep = dto.getCurrentStep() + 1;
  
            // Optional: Add a limit so it doesn't go past your max step (e.g., 5)
            if (nextStep > 5) {
                nextStep = 5; 
            }
            
            tournament.setCurrentStep(nextStep);
        }

       if ("PUBLISH".equals(request.getParameter("action"))) {

            User user = securityUtils.getLoggedInUser();


            OrganizerProfile organizerProfile = organizerService.getByUser(user);


            if(organizerProfile == null || !organizerProfile.isIdentityVerified()){
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "requiresIdentity", true, // Frontend uses this flag
                    "message", "Identity verification is required to publish a tournament. Please complete your profile."
                ));
            }
            // Both are finished!
            tournament.setSetupStatus(TournamentRegistrationStatus.COMPLETE);
            tournament.setStatus(TournamentStatus.REGISTRATION_OPEN); 
        } else {
            // Organizer stopped early or saved for later
            tournament.setSetupStatus(TournamentRegistrationStatus.INCOMPLETE);
            tournament.setStatus(TournamentStatus.UNPUBLISHED);
        }

        // finally save the tournament into the db
        Tournament savedTournament = tournamentService.saveTournament(tournament);

        return ResponseEntity.ok(Map.of(
            "success", true,
            "nextUrl", "/api/organizer/tournament/create-tournament/step-" + savedTournament.getCurrentStep() + "?id=" + savedTournament.getId()
        ));
    }

    
    @GetMapping("/create-tournament/step-{step}")
    public String getTournamentStep(@PathVariable Integer step,
      @RequestParam(required=false) Long id,
      Model model
    ){

       User organizer =  securityUtils.getLoggedInUser();

      if(organizer==null){
          return "redirect:/login";
      }

      OrganizerProfile profile = organizerService.getByUser(organizer);

      model.addAttribute("organizerProfile",profile);

      Tournament tournament;

      if (id != null) {
          tournament = tournamentService.getTournamentId(id);
          if (tournament == null) {
              return "redirect:/api/organizer/tournament/new";
          }
      } else {
          tournament = new Tournament();
          tournament.setSetupStatus(TournamentRegistrationStatus.INCOMPLETE);
          tournament.setStatus(TournamentStatus.UNPUBLISHED);
          tournament.setOrganizer(profile);
          tournament.setCurrentStep(step);
      }

      if (tournament.getCurrentStep() == null || tournament.getCurrentStep() < step) {
        tournament.setCurrentStep(step);
        tournamentService.saveTournament(tournament);
      }

      model.addAttribute("tournamentDTO", tournamentMapper.toDTO(tournament));
      model.addAttribute("activeStep", step);

      return "organizer/createTournament";
    }

   @GetMapping("/myTournaments")
    public String myTournament(
            HttpServletRequest req,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sport,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = AppConstants.PAGE_SIZE) int size,
            Model model) {

        User organizer = securityUtils.getLoggedInUser();

        if (organizer == null) {
            return "redirect:/login";
        }

        OrganizerProfile profile = organizerService.getByUser(organizer);

        if (profile == null) {
            return "redirect:/login";
        }

        model.addAttribute("organizerProfile", profile);

        // 1. Get metrics for stats cards
        List<Tournament> rawTournaments = tournamentService.getTournamentsByOrganizer(profile.getId());
        List<Tournament> myTournaments = rawTournaments.stream()
                .filter(t -> t != null)
                .collect(Collectors.toList());

        long totalCount = myTournaments.size();
        long openCount = myTournaments.stream().filter(t -> t.getStatus() == TournamentStatus.REGISTRATION_OPEN).count();
        long ongoingCount = myTournaments.stream().filter(t -> t.getStatus() == TournamentStatus.ONGOING).count();
        long completedCount = myTournaments.stream().filter(t -> t.getStatus() == TournamentStatus.COMPLETED).count();
        long draftCount = myTournaments.stream().filter(t -> t.getStatus() == TournamentStatus.UNPUBLISHED).count();
        
        long upcomingCount = myTournaments.stream().filter(t -> t.getStatus() == TournamentStatus.REGISTRATION_OPEN 
                && t.getStartDate() != null && t.getStartDate().isAfter(LocalDate.now())).count();

        double totalRevenue = myTournaments.stream()
            .mapToDouble(t -> t.getTotalRevenue() != null ? t.getTotalRevenue() : 0)
            .sum();

        model.addAttribute("totalCount", totalCount);
        model.addAttribute("openCount", openCount);
        model.addAttribute("ongoingCount", ongoingCount);
        model.addAttribute("completedCount", completedCount);
        model.addAttribute("draftCount", draftCount);
        model.addAttribute("upcomingCount", upcomingCount);         
        model.addAttribute("totalRevenue", totalRevenue);

        // 2. Build Specification with search/filter criteria
        final Long profileId = profile.getId();
        Specification<Tournament> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            Join<Tournament, OrganizerProfile> organizerJoin = root.join("organizer");
            predicates.add(cb.equal(organizerJoin.get("id"), profileId));

            if (keyword != null && !keyword.trim().isEmpty()) {
                String pattern = "%" + keyword.toLowerCase().trim() + "%";
                Predicate nameMatch = cb.like(cb.lower(root.get("tournamentName")), pattern);
                Predicate locationMatch = cb.like(cb.lower(root.get("venueLocation")), pattern);
                predicates.add(cb.or(nameMatch, locationMatch));
            }
            
            if (sport != null && !sport.trim().isEmpty()) {
                predicates.add(cb.equal(root.get("sport"), sport.trim()));
            }

            if (status != null && !status.trim().isEmpty() && !status.equalsIgnoreCase("ALL")) {
                try {
                    String formattedStatus = status.trim().toUpperCase().replace(" ", "_");
                    TournamentStatus statusEnum = TournamentStatus.valueOf(formattedStatus);
                    predicates.add(cb.equal(root.get("status"), statusEnum));
                } catch (IllegalArgumentException e) {
                    System.err.println("DEBUG: Invalid status provided: " + status);
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Pageable pageable = PageRequest.of(page, size, Sort.by("startDate").descending());
        Page<Tournament> tournamentPage = tournamentService.findAll(spec, pageable);

        model.addAttribute("myTournaments", tournamentPage.getContent());
        model.addAttribute("currentPage", tournamentPage.getNumber());
        model.addAttribute("totalPages", tournamentPage.getTotalPages());
        model.addAttribute("totalItems", tournamentPage.getTotalElements());
        model.addAttribute("hasNext", tournamentPage.hasNext());
        model.addAttribute("hasPrevious", tournamentPage.hasPrevious());

        // 3. Check if AJAX request -> return ONLY the fragment view
        String header = req.getHeader("X-Requested-With");
        if ("XMLHttpRequest".equals(header)) {
            return "fragments/tournament_list :: tournament-grid";
        }

        return "tournament/myTournament";
    }

    @GetMapping("/search")
    public String searchTournaments(
        HttpServletRequest req,
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String sport,
        @RequestParam(required = false) String status,
        @RequestParam(defaultValue="0") int page,
        @RequestParam(defaultValue=AppConstants.PAGE_SIZE) int size, 
        Model model) {
        
        System.out.println("DEBUG: Searching for -> Keyword: " + keyword + ", Sport: " + sport + ", Status: " + status + ", Page: " + page + ", Size: " + size);
        
        User organizer = securityUtils.getLoggedInUser();

        if(organizer == null){
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if(auth != null && auth.getPrincipal() instanceof User){
                organizer = (User) auth.getPrincipal();
                req.getSession().setAttribute("loggedInUser", organizer);
            }
            else {
                return "redirect:/login";
            }
        }

        OrganizerProfile profile = organizerService.getByUser(organizer);

        // Safety check: if profile is null, return empty view gracefully without throwing NPE
        if (profile == null) {
            System.err.println("DEBUG: OrganizerProfile is null for user: " + organizer.getEmail());
            model.addAttribute("myTournaments", new ArrayList<>());
            model.addAttribute("currentPage", 0);
            model.addAttribute("totalPages", 0);
            model.addAttribute("totalItems", 0L);
            model.addAttribute("hasNext", false);
            model.addAttribute("hasPrevious", false);
            return "fragments/tournament_list :: tournament-grid";
        }

        final Long profileId = profile.getId();

        // Build Specification safely using final profileId
        Specification<Tournament> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            Join<Tournament, OrganizerProfile> organizerJoin = root.join("organizer");
            predicates.add(cb.equal(organizerJoin.get("id"), profileId));

            if (keyword != null && !keyword.trim().isEmpty()) {
                String pattern = "%" + keyword.toLowerCase().trim() + "%";
                Predicate nameMatch = cb.like(cb.lower(root.get("tournamentName")), pattern);
                Predicate locationMatch = cb.like(cb.lower(root.get("venueLocation")), pattern);
                predicates.add(cb.or(nameMatch, locationMatch));
            }
            
            if (sport != null && !sport.trim().isEmpty()) {
                predicates.add(cb.equal(root.get("sport"), sport.trim()));
            }

            if (status != null && !status.trim().isEmpty() && !status.equalsIgnoreCase("ALL")) {
                try {
                    String formattedStatus = status.trim().toUpperCase().replace(" ", "_");
                    TournamentStatus statusEnum = TournamentStatus.valueOf(formattedStatus);
                    predicates.add(cb.equal(root.get("status"), statusEnum));
                } catch (IllegalArgumentException e) {
                    System.err.println("DEBUG: Invalid status provided: " + status);
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Pageable pageable = PageRequest.of(page, size, Sort.by("startDate").descending());
        Page<Tournament> tournamentPage = tournamentService.findAll(spec, pageable);

        model.addAttribute("myTournaments", tournamentPage.getContent());
        model.addAttribute("currentPage", tournamentPage.getNumber());
        model.addAttribute("totalPages", tournamentPage.getTotalPages());
        model.addAttribute("totalItems", tournamentPage.getTotalElements());
        model.addAttribute("hasNext", tournamentPage.hasNext());
        model.addAttribute("hasPrevious", tournamentPage.hasPrevious());
        
        return "fragments/tournament_list :: tournament-grid";
    }

    @GetMapping("/search-tournament")
    public String searchTournament(
            HttpServletRequest req,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sport,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = AppConstants.PAGE_SIZE) int size,
            Model model) {
        
        System.out.println("DEBUG: Searching for -> Keyword: " + keyword + ", Sport: " + sport + ", Status: " + status + ", Page: " + page + ", Size: " + size);
        
        User organizer = securityUtils.getLoggedInUser();
        if (organizer == null) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof User) {
                organizer = (User) auth.getPrincipal();
            }
        }

        // Specification for filtering tournaments safely
        Specification<Tournament> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // Always exclude unpublished tournaments for public viewing
            predicates.add(cb.notEqual(root.get("status"), TournamentStatus.UNPUBLISHED));

            // Keyword filter: search name or location
            if (keyword != null && !keyword.trim().isEmpty()) {
                String pattern = "%" + keyword.toLowerCase().trim() + "%";
                Predicate nameMatch = cb.like(cb.lower(root.get("tournamentName")), pattern);
                Predicate locationMatch = cb.like(cb.lower(root.get("venueLocation")), pattern);
                predicates.add(cb.or(nameMatch, locationMatch));
            }
            
            // Sport filter
            if (sport != null && !sport.trim().isEmpty() && !sport.equalsIgnoreCase("all")) {
                predicates.add(cb.equal(cb.lower(root.get("sport")), sport.toLowerCase().trim()));
            }

            // Status filter
            if (status != null && !status.trim().isEmpty() && !status.equalsIgnoreCase("ALL") && !status.equalsIgnoreCase("all")) {
                try {
                    String formattedStatus = status.trim().toUpperCase().replace(" ", "_");
                    TournamentStatus statusEnum = TournamentStatus.valueOf(formattedStatus);
                    predicates.add(cb.equal(root.get("status"), statusEnum));
                } catch (IllegalArgumentException e) {
                    System.err.println("DEBUG: Invalid status provided: " + status);
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // Create pageable instance using AppConstants.PAGE_SIZE
        Pageable pageable = PageRequest.of(page, size, Sort.by("startDate").ascending());

        Page<Tournament> tournamentPage = tournamentService.findAll(spec, pageable);

        System.out.println("DEBUG: Found " + (tournamentPage != null ? tournamentPage.getTotalElements() : 0) + " total tournaments matching filters.");

        // Add page content and pagination metadata safely
        if (tournamentPage != null) {
            model.addAttribute("tournaments", tournamentPage.getContent());
            model.addAttribute("currentPage", tournamentPage.getNumber());
            model.addAttribute("totalPages", tournamentPage.getTotalPages());
            model.addAttribute("totalItems", tournamentPage.getTotalElements());
            model.addAttribute("hasNext", tournamentPage.hasNext());
            model.addAttribute("hasPrevious", tournamentPage.hasPrevious());
        } else {
            model.addAttribute("tournaments", new ArrayList<>());
            model.addAttribute("currentPage", 0);
            model.addAttribute("totalPages", 0);
            model.addAttribute("totalItems", 0L);
            model.addAttribute("hasNext", false);
            model.addAttribute("hasPrevious", false);
        }
        
        return "fragments/List_Tournament :: tournament-grid";
    }

    private boolean isAnyFilterApplied(String keyword, String sport, String status) {
        boolean hasKeyword = (keyword != null && !keyword.trim().isEmpty());
        boolean hasSport = (sport != null && !sport.trim().isEmpty());
        boolean hasStatus = (status != null && !status.trim().isEmpty() && !status.equals("ALL"));
        
        System.out.println("DEBUG: Filter Check -> Keyword: " + hasKeyword + ", Sport: " + hasSport + ", Status: " + hasStatus);
        return hasKeyword || hasSport || hasStatus;
    }

    @GetMapping("/manage/{id}")
    public String showManageTournament(@PathVariable("id") Long id, Model model) {

        
        User user = securityUtils.getLoggedInUser();
    
        if (user == null) {
            return "redirect:/login";
        }
        
        System.out.println("The user Name is  : "+user.getEmail());
        System.out.println("The Tournament ID is   : "+id);


        OrganizerProfile organizerProfile = organizerService.getByUser(user);

        if(organizerProfile ==null)  System.out.println("Organizer Null in Manage routee");

        System.out.println("The Manage routte Organizer name is : "+organizerProfile.getOrganizationName());

        
        if (organizerProfile != null){
            List<NotificationDTO> notifications = notificationService.getLiveNotificationsForOrganizer(organizerProfile.getId());
            model.addAttribute("notifications", notifications);
        }
        model.addAttribute("organizerProfile", organizerProfile);

        // Take a tournament based on the id..}
        Tournament tournament = tournamentService.getTournamentId(id);
        model.addAttribute("tournament", tournament);

        // Gather all teams registered for this tournament..
        List<TeamRegistration> Teams = teamRegistrationService.getTeamsByTournament(tournament);

        // Filter out REJECTED teams
        List<TeamRegistration> activeTeams = Teams.stream()
                .filter(team -> team.getStatus() != RegistrationTeamStatus.REJECTED)
                .collect(Collectors.toList());

        System.out.println("The teams size is : " + Teams.size());
        model.addAttribute("teams", activeTeams);

        List<Match> matches = matchService.getTeamsByTournamentId(tournament.getId());

        // --- GROUP MATCHES DYNAMICALLY BY ACTUAL MATCH DATE ---
        Map<LocalDate, List<Match>> groupedByDate = matches.stream()
            .filter(m -> m.getMatchDate() != null)
            .collect(Collectors.groupingBy(
                Match::getMatchDate, 
                TreeMap::new, 
                Collectors.toList()
            ));

        List<Map<String, Object>> roundsList = new ArrayList<>();

        for (Map.Entry<LocalDate, List<Match>> entry : groupedByDate.entrySet()) {
            Map<String, Object> dateMap = new HashMap<>();
            dateMap.put("roundName", entry.getKey().toString()); // Acts as the date header on UI
            dateMap.put("fixtures", entry.getValue());
            roundsList.add(dateMap);
        }   

        if (matchService.existsByTournamentId(tournament.getId())) {
            System.out.println("Foundd");
        } else {
            System.out.println("Tournament Id is Not Foundd...");
        }

        model.addAttribute("rounds", roundsList);

        // Schedule Section
        int totalMatches = matches.size();
        
        long totalRounds = matches.stream()
                .map(Match::getRoundNumber)
                .distinct()
                .count();

        long scheduledMatchesCount = matches.stream()
                .filter(m -> m.getMatchDate() != null && m.getMatchTime() != null)
                .count();

        long unscheduledMatchesCount = totalMatches - scheduledMatchesCount;

        int scheduledPercentage = totalMatches > 0 ? (int) Math.round(((double) scheduledMatchesCount / totalMatches) * 100) : 0;

        // --- DEBUG MATCHES ---
        for (Match m : matches) {
            System.out.println("DEBUG MATCH -> ID: " + m.getId() + 
                               " | Date: " + m.getMatchDate() + 
                               " | Time: " + m.getMatchTime() + 
                               " | Venue: [" + m.getVenueLocation() + "]");
        }

        // --- RELIABLE IN-MEMORY CONFLICT DETECTION ---
        long conflictsCount = 0;
        Map<Long, String> conflictMessages = new HashMap<>();

        for (int i = 0; i < matches.size(); i++) {
            for (int j = i + 1; j < matches.size(); j++) {
                Match m1 = matches.get(i);
                Match m2 = matches.get(j);

                if (m1.getMatchDate() != null && m2.getMatchDate() != null &&
                    m1.getMatchTime() != null && m2.getMatchTime() != null &&
                    m1.getVenueLocation() != null && m2.getVenueLocation() != null) {

                    if (m1.getMatchDate().equals(m2.getMatchDate()) &&
                        m1.getMatchTime().equals(m2.getMatchTime()) &&
                        m1.getVenueLocation().trim().equalsIgnoreCase(m2.getVenueLocation().trim())) {

                        conflictsCount++;

                        Match earlierMatch = m1.getId() < m2.getId() ? m1 : m2;
                        Match laterMatch = m1.getId() < m2.getId() ? m2 : m1;

                        String msg = String.format("%s is already occupied by Match #%d at this time.", 
                            laterMatch.getVenueLocation(), 
                            earlierMatch.getMatchNumber() != null ? earlierMatch.getMatchNumber() : earlierMatch.getId()
                        );
                        
                        conflictMessages.put(laterMatch.getId(), msg);

                        System.out.println("🚨 CONFLICT DETECTED!");
                        System.out.println("   - Venue: " + laterMatch.getVenueLocation());
                        System.out.println("   - Date & Time: " + laterMatch.getMatchDate() + " @ " + laterMatch.getMatchTime());
                        System.out.println("   - Earlier Match ID/No: " + (earlierMatch.getMatchNumber() != null ? earlierMatch.getMatchNumber() : earlierMatch.getId()));
                        System.out.println("   - Conflicting Later Match ID/No: " + (laterMatch.getMatchNumber() != null ? laterMatch.getMatchNumber() : laterMatch.getId()));
                        System.out.println("--------------------------------------------------");
                    }
                }
            }
        } 

        // To check whether the match status is change based on the data
        // SCHEDULED, LIVE 
        LocalDateTime now = LocalDateTime.now();
        for (Match m : matches) {
            if (m.getMatchDate() != null && m.getMatchTime() != null) {
                LocalDateTime matchDateTime = LocalDateTime.of(m.getMatchDate(), m.getMatchTime());

                // 1. If the match time is in the future, it is strictly SCHEDULED
                if (now.isBefore(matchDateTime)) {
                    m.setStatus(MatchStatus.SCHEDULED);
                }
                // 2. If it's currently happening (within 3 hours after start time)
                else if (now.isAfter(matchDateTime) && now.isBefore(matchDateTime.plusHours(3))) {
                    // Check if scores are entered, otherwise show LIVE
                    if (m.getTeamAScore() != null && m.getTeamBScore() != null) {
                        m.setStatus(MatchStatus.COMPLETED);
                    } else {
                        m.setStatus(MatchStatus.LIVE);
                    }
                } 
                // 3. If it's past the match window (> 3 hours ago)
                else {
                    m.setStatus(MatchStatus.COMPLETED);
                }
            }
        }


        String activeDateStr = LocalDate.now().toString();
        for (Match m : matches) {
            if (m.getMatchDate() != null) {
                activeDateStr = m.getMatchDate().toString();
                break; 
            }
        } 
        System.out.println("Date is : " + activeDateStr);

       

        // Add all attributes to the Model
        model.addAttribute("tournament", tournament);
        model.addAttribute("scheduleMatches", matches);
        model.addAttribute("totalMatches", totalMatches);
        model.addAttribute("totalRounds", totalRounds == 0 ? 1 : totalRounds);
        model.addAttribute("scheduledMatchesCount", scheduledMatchesCount);
        model.addAttribute("scheduledPercentage", scheduledPercentage);
        model.addAttribute("unscheduledMatchesCount", unscheduledMatchesCount);
        model.addAttribute("conflictsCount", conflictsCount);
        model.addAttribute("activeDateFormatted", activeDateStr);
        model.addAttribute("conflictMessages", conflictMessages);

        return "tournament/manageTournament";
    }

    @GetMapping("/team/{id}/players")
    @ResponseBody
    public ResponseEntity<?> getTeamPlayersForOrganizer(@PathVariable("id") Long id) {
        
       try{
            List<TeamRoster> roster = teamRosterService.getByTeamRegistrationId(id);

            // FIX: send flat DTOs instead of entities. The entities loop back on
        // themselves (PlayerProfile -> User -> PlayerProfile), which broke the
        // JSON ("nesting depth exceeds 1000") and caused "Failed to load players".

        List<RosterEntryDTO> rosterDto = roster.stream()
            .filter(r -> r.getPlayerProfile() != null)
            .map(r -> {
                PlayerProfile p = r.getPlayerProfile();
                return new RosterEntryDTO(
                    r.getId(),
                    r.getRoleInTeam() != null ? r.getRoleInTeam().name() : "PLAYER",
                    new PlayerDetailDTO(
                        p.getId(),
                        p.getFullName(),
                        p.getProfileImage(),
                        p.getPlayerIdProof(),
                        p.getIdType(),
                        p.getIdNumber(),
                        p.isVerified()));
            })
            .toList();

            TeamRegistration team = teamRegistrationService.getTeamRegistrationById(id).orElse(null);
            
            String status = (team != null) ? team.getStatus().name() : "PENDING";


            Map<String,Object> response = new HashMap<>();
            response.put("status",status);
            response.put("roster", rosterDto);

            return ResponseEntity.ok(response);
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body("Error fetching players: " + e.getMessage());
        }
    }

    @PostMapping("/team/{teamId}/approve")
    public ResponseEntity<?> approveTeam(@PathVariable Long teamId){

        try{
            TeamRegistration team = teamRegistrationService.getTeamRegistrationById(teamId).orElse(null);

            if (team == null) {
                return ResponseEntity.status(404).body("Team not found");
            }

            team.setStatus(RegistrationTeamStatus.APPROVED);

            teamRegistrationService.saveTeam(team); // or teamRegistrationRepo.save(team)

            return ResponseEntity.ok("Team approved successfully!");
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to approve team: " + e.getMessage());
        }
    }


    @PostMapping("/team/{teamId}/reject")
    public ResponseEntity<?> rejectTeam(@PathVariable Long teamId){

        try{
            TeamRegistration team = teamRegistrationService.getTeamRegistrationById(teamId).orElse(null);

            if (team == null) {
                return ResponseEntity.status(404).body("Team not found");
            }

            int squad_Size = team.getSquadSize();

            squad_Size -= 1;
            team.setSquadSize(squad_Size);
            team.setStatus(RegistrationTeamStatus.REJECTED);

            teamRegistrationService.saveTeam(team); // or teamRegistrationRepo.save(team)   

            return ResponseEntity.ok("Team is Rejected...");
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to Reject team: " + e.getMessage());
        }
    }

    @PostMapping("/{tournamentId}/generate-fixtures")
    public ResponseEntity<?> generatureFixtures(@PathVariable("tournamentId") Long tournamentId){
        try{


            if(matchService.existsByTournamentId(tournamentId)){
                throw new RuntimeException("Fixtures have alredy been generated...");
            }

            tournamentService.generateFixturesForTournament(tournamentId);

            return ResponseEntity.ok("Fixtures generated successfully for tournament ID: " + tournamentId);
        }
        catch (IllegalArgumentException | IllegalStateException e) {
            // Catch validation errors (e.g., not enough teams, or fixtures already generated)
            return ResponseEntity.badRequest().body(e.getMessage());
            
        } catch (Exception e) {
            // Catch any unexpected server errors
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body("An error occurred while generating fixtures: " + e.getMessage());
        }
    }
    
    @PostMapping("/{tournamentId}/manual-fixtures")
    public ResponseEntity<?> storeManualTeam(@PathVariable Long tournamentId,   @RequestBody ManualFixturesRequestDto requestDto) throws JsonProcessingException{
        System.out.println("Manual Team Called...");

        System.out.println("RAW JSON RECEIVED: " + new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(requestDto));

        // Print incoming data to the console
            if (requestDto != null && requestDto.getFixtures() != null) {
            System.out.println("Received fixtures count: " + requestDto.getFixtures().size());
            for (int i = 0; i < requestDto.getFixtures().size(); i++) {
                ManualFixtureItemDto fixture = requestDto.getFixtures().get(i);
                System.out.println("--- Fixture " + i + " ---");
                System.out.println("Round Number: " + fixture.getRoundNumber());
                System.out.println("Match Date: " + fixture.getMatchDate());
                System.out.println("Match Time: " + fixture.getMatchTime());
                System.out.println("Status: " + fixture.getStatus());
            }
        } else {
                System.out.println("requestDto or fixtures list is NULL!");
            }
        try {
            tournamentService.saveManualFixturesBatch(tournamentId, requestDto);
            return ResponseEntity.ok("Manual fixtures saved successfully!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error saving fixtures: " + e.getMessage());
        }
    }

    @PostMapping("/publish/{tournamentId}")
    public ResponseEntity<?> publishTournamentSchedule(@PathVariable Long tournamentId){
        try{
            Tournament tournament = tournamentService.getTournamentId(tournamentId);
            String organizerName = tournament.getOrganizer().getOrganizationName(); // Access it here!
            String organizerEmail = tournament.getOrganizer().getUser().getEmail();
            // 1. Perform your database logic to mark schedule as published
            // Example: tournamentService.setTournamentPublished(tournamentId, true);
            
            // 2. Trigger asynchronous email alerts
            emailNotificationService.sendSchedulePublishedEmailToCaptains(tournamentId,organizerName,organizerEmail);

            tournament.setSchedulePublished(true);
            tournamentService.saveTournament(tournament);

            // 3. Return a success response that your JavaScript 'fetch' call will receive
            return ResponseEntity.ok(Map.of(
                "success", true, 
                "message", "Schedule published and emails sent successfully!"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false, 
                "message", "Error publishing schedule: " + e.getMessage()
            ));
        }
    }

    // @PathVariable("tournamentId") Long tournamentId, Model model, Principal principal
    @GetMapping("{tournamentId}/live-scoring")
    public String showLiveScoringPage(@PathVariable("tournamentId") Long tournamentId, Model model) {

        Tournament tournament = tournamentService.getTournamentId(tournamentId);
        model.addAttribute("tournament", tournament);

        User user = securityUtils.getLoggedInUser();
    
        if (user == null) {
            return "redirect:/login";   
        }

        OrganizerProfile organizerProfile = organizerService.getByUser(user);
        if(organizerProfile ==null)  System.out.println("Organizer Null in Live Score");
        System.out.println("The Live Score Organizer name is : "+organizerProfile.getOrganizationName());
        model.addAttribute("organizerProfile", organizerProfile);

        System.out.println("The Tournament id In Live is : "+tournament.getId());
        
        List<Match> matchesManage = matchService.getAllByTournamentOrderedByStatusPriority(tournament.getId());
        model.addAttribute("matchesManage",matchesManage);

       if(matchesManage==null) System.out.println("Mathes are null..");
       else System.out.println("Mathes are not null..");

        model.addAttribute("scoringAccessDTO", new ScoringAccessDTO());

        System.out.println("The teams are : ");
        for(Match m : matchesManage){
            System.out.println(m.getTeamA().getTeamName());
            System.out.println(m.getTeamB().getTeamName());
        }
        return "fragments/liveScoringContent :: content";
    }

    
    
}
