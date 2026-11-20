package com.champions_Call.Controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.champions_Call.Form.Player.PlayerProfileDTO;
import com.champions_Call.model.Team.TeamRegistration;
import com.champions_Call.model.Team.TeamRoster;
import com.champions_Call.model.User;
import com.champions_Call.model.player.PlayerProfile;
import com.champions_Call.model.player.Strength;
import com.champions_Call.repository.StrengthRepository;
import com.champions_Call.service.ImageService;
import com.champions_Call.service.PlayerService;
import com.champions_Call.service.SecurityUtils;
import com.champions_Call.service.TeamRegistrationService;
import com.champions_Call.service.TeamRosterService;
import com.champions_Call.service.TournamentService;

@Controller
@RequestMapping("/api/player")
public class playerController{

    @Autowired
    private StrengthRepository strengthRepo;

    @Autowired 
    private PlayerService playerService;

    @Autowired
    private SecurityUtils securityUtils;

    @Autowired
    private ImageService imageService;

    @Autowired
    private TeamRegistrationService teamRegistrationService;

    @Autowired
    private TeamRosterService teamRosterService;

    @Autowired
    private TournamentService tournamentService;


    // Standard for loading the page
    @GetMapping("/setup")
    public String showProfileSetupForm(Model model) {



        model.addAttribute("playerProfile", new PlayerProfileDTO());

        // Fetch all strengths and pass them to the template
        List<Strength> allStrengths = strengthRepo.findAll();
        model.addAttribute("allStrengths", allStrengths);

        return "player/playerProfileCompletion"; // Returning the view name
    }

    @PostMapping("/save-step")
    @ResponseBody
    public ResponseEntity<?> savePlayerProfile(
        @ModelAttribute("playerProfile") PlayerProfileDTO dto,
        @RequestParam(value="currentStep",defaultValue="1") Integer currentStep
    ){
        System.out.println("Player Profile is called...");

        System.out.println("Player full Name is : "+dto.getFullName());
        System.out.println("Player Primary Sport is : "+dto.getPrimarySport());
        System.out.println("Player Skill Level is : "+dto.getSkillLevel());
        System.out.println("Player experience is : "+dto.getYearsOfExperience());
        System.out.println("Player bio : "+dto.getBio());
        System.out.println("Player current Status: "+dto.getCurrentStatus());


        String username = dto.getUsername();

        // get the Current LoggedIn user

        User user = securityUtils.getLoggedInUser();

        PlayerProfile playerProfile = playerService.getPlayerProfileByUser(user)
                                    .orElse(new PlayerProfile());

                                    

        // If it's a new profile, set the user because if 
        // a user is already so we just update the existing one..
        if (playerProfile.getUser() == null) {
            playerProfile.setUser(user);
        }  

       // Check Username
        if (playerProfile.getUsername() == null || !playerProfile.getUsername().equals(dto.getUsername())) {
            if (playerService.isUsernameExist(dto.getUsername())) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false, 
                    "message", "This Username is already taken! Please try another one."
                ));
            }
        }
        if (dto.getPhoneNumber() != null && !dto.getPhoneNumber().equals(playerProfile.getPhoneNumber())) {
            // Only check existence if the number is different from what's currently in their profile
            if (playerService.isPhoneNumberExist(dto.getPhoneNumber())) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false, 
                    "message", "This Phone Number is already registered by another account!"
                ));
            }
        }

        String fileName = UUID.randomUUID().toString();
        

        if(dto.getProfileImage() != null && !dto.getProfileImage().isEmpty()){
            String fileUrl = imageService.uploadImage(dto.getProfileImage(),fileName);
            playerProfile.setProfileImage(fileUrl);
            playerProfile.setCloudinaryImagePublicId(fileName);
        }
        else{
            String initials = generateInitials(dto.getFullName());
            String defaultAvatarUrl = "https://ui-avatars.com/api/?"
                    + "name=" + initials 
                    + "&background=151518" 
                    + "&color=ff914d" 
                    + "&size=200" 
                    + "&bold=true";

            playerProfile.setProfileImage(defaultAvatarUrl);
            playerProfile.setCloudinaryImagePublicId(null);
        }

        if(dto.getFullName() != null && !dto.getFullName().isEmpty()){
            playerProfile.setFullName(dto.getFullName());
        }

        if(dto.getUsername() != null && !dto.getUsername().isEmpty()){
            playerProfile.setUsername(dto.getUsername());
        }

        if(dto.getDateOfBirth() != null){
            playerProfile.setDateOfBirth(dto.getDateOfBirth());
        }

        if(dto.getCity() != null && !dto.getCity().isEmpty()){
            playerProfile.setCity(dto.getCity());
        }

        if(dto.getPhoneNumber() != null && !dto.getPhoneNumber().isEmpty()){
            playerProfile.setPhoneNumber(dto.getPhoneNumber());
        }

        if(dto.getPrimarySport() != null && !dto.getPrimarySport().isEmpty()){
            playerProfile.setPrimarySport(dto.getPrimarySport());
        }

        if(dto.getPlayerRole() != null && !dto.getPlayerRole().isEmpty()){
            playerProfile.setPlayerRole(dto.getPlayerRole());
        }

        if(dto.getSkillLevel() != null && !dto.getSkillLevel().isEmpty()){
            playerProfile.setSkillLevel(dto.getSkillLevel());
        }

        if(dto.getPreferredSide() != null && !dto.getPreferredSide().isEmpty()){
            playerProfile.setPreferredSide(dto.getPreferredSide());
        }

        if (dto.getCurrentStatus() != null) {
            playerProfile.setCurrentStatus(dto.getCurrentStatus());
        }
        playerProfile.setCurrentStep(currentStep);

        if(dto.getBio() != null && !dto.getBio().isEmpty()){
            playerProfile.setBio(dto.getBio());
        }
        if(dto.getYearsOfExperience() != null){
            playerProfile.setYearsOfExperience(dto.getYearsOfExperience());
        }

        if(dto.getOtherStrength() != null && !dto.getOtherStrength().isEmpty()){
            playerProfile.setOtherStrength(dto.getOtherStrength());
        }

        // 5. STORE STRENGTHS
        if (dto.getStrengthIds() != null && !dto.getStrengthIds().isEmpty()) {
            Set<Strength> strengths = strengthRepo.findAllById(dto.getStrengthIds())
                        .stream()
                        .collect(Collectors.toSet());
            playerProfile.setStrengths(strengths);
        }

        // step 3 : ID Proof
        if(dto.getIdType() != null && !dto.getIdType().isEmpty()){
            playerProfile.setIdType(dto.getIdType());
        }

        if(dto.getIdNumber() != null && !dto.getIdNumber().isEmpty()){
            playerProfile.setIdNumber(dto.getIdNumber());
        }
        // Handle ID Document File Upload (e.g. to Cloudinary or your local storage service)
        if (dto.getIdDocument() != null && !dto.getIdDocument().isEmpty()) {
            String docFileName = "id_doc_" + UUID.randomUUID().toString();
            String docFileUrl = imageService.uploadImage(dto.getIdDocument(), docFileName);
            playerProfile.setPlayerIdProof(docFileUrl);
        }
        // 6. Set Completion Flag
        if (currentStep == 3) {
            playerProfile.setCompleteProfile(true);
        }
    
        // 7. SAVE TO DATABASE
        playerService.savePlayerProfile(playerProfile); 

        System.out.println("Player Profile is Saved Successfully...");

        return ResponseEntity.ok(Map.of("success", true, "message", "Saved successfully"));
    }

    private String generateInitials(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "CC"; // Fallback abbreviation if completely empty
        }
        String[] parts = name.trim().split("\\s+");
        StringBuilder initials = new StringBuilder();
        
        // Grab the first letter of the first word
        if (parts.length > 0 && !parts[0].isEmpty()) {
            initials.append(parts[0].charAt(0));
        }
        // Grab the first letter of the last word (if there are multiple words)
        if (parts.length > 1 && !parts[parts.length - 1].isEmpty()) {
            initials.append(parts[parts.length - 1].charAt(0));
        }
        
        return initials.toString().toUpperCase();
    }

    @GetMapping("/profile")
    public String showProfile(Model model){

        

        User user = securityUtils.getLoggedInUser();

        if (user == null) {
            return "redirect:/login";
        }

        PlayerProfile player = playerService.getPlayerProfileByUser(user)
                                    .orElse(new PlayerProfile());

        int count = player.getTotalMatches();

        // If it's a new profile, set the user
        if (player.getUser() == null) {
            player.setUser(user);
        }  

        // DEBUG PRINTS TO CHECK VALUES IN CONSOLE
        System.out.println("=== PLAYER PROFILE STATUS DEBUG ===");
        System.out.println("Player ID Proof URL/Text: " + player.getPlayerIdProof());
        System.out.println("Is Verified?: " + player.isVerified());
        System.out.println("===================================");


        String email = user.getEmail();
        System.out.println("The Player Email is : "+email);
        model.addAttribute("playerEmail", email);
        model.addAttribute("profile", player);

        List<TeamRoster> playerTeams = teamRosterService.getTeamsForPlayer(player);

        boolean isManager = playerTeams.stream()
        .anyMatch(roster -> roster.getRoleInTeam() != null && 
            (roster.getRoleInTeam().name().equals("CAPTAIN") || roster.getRoleInTeam().name().equals("VICE_CAPTAIN")));

        model.addAttribute("isManager", isManager);
        model.addAttribute("managedTeams", playerTeams);

        System.out.println("Teams found: " + playerTeams.size());

        // Check if the logged-in player is a CAPTAIN in ANY of their managed teams
        boolean isCaptain = playerTeams.stream()
                .anyMatch(roster -> roster.getRoleInTeam() != null && roster.getRoleInTeam().name().equals("CAPTAIN"));

        if(isCaptain) System.out.println("Captain...");
        else System.out.println("Not Captain...");

        model.addAttribute("isCaptain", isCaptain);


        // 1. Safely extract the teamRegId from the player's team (e.g., the first team in their list)
        if (!playerTeams.isEmpty() && playerTeams.get(0).getTeamRegistration() != null) {
            Map<Long, List<TeamRoster>> squad = new HashMap<>();
            for (TeamRoster roster : playerTeams) {
                TeamRegistration tr = roster.getTeamRegistration();
                if (tr != null && !squad.containsKey(tr.getId())) {
                    List<TeamRoster> members = teamRegistrationService.getSortedTeamSquad(tr.getId());
                    squad.put(tr.getId(), members);

                    System.out.println("Team: " + tr.getTeamName()
                        + " (ID " + tr.getId() + ") -> roster size: " + members.size());
                            }
                        }
            model.addAttribute("squad", squad);
        } else {
            model.addAttribute("squad", new java.util.ArrayList<>());
        }


        // finding all tournament that a player played...
       List<TeamRegistration> playedTournaments = playerTeams.stream()
            .map(TeamRoster::getTeamRegistration)
            .filter(tr -> tr != null)
            .distinct()
            .toList();

        System.out.println("The Tournaments size : " + playedTournaments.size());

        model.addAttribute("playedTournaments", playedTournaments);


        return "player/playerProfile";
    }

    @GetMapping("/profile/{id}")
    public String viewPlayerProfileId(@PathVariable("id") Long id, Model model){

        User user = securityUtils.getLoggedInUser();

        if(user == null){
            return "redirect:/login";
        }

        PlayerProfile profile = playerService.getPlayerProfileById(id);

        model.addAttribute("profile", profile);
        model.addAttribute("playerEmail", user.getEmail());

        List<TeamRoster> playerTeams = teamRosterService.getTeamsForPlayer(profile);
        model.addAttribute("managedTeams", playerTeams);

        // Check if the viewed profile is a CAPTAIN in any of those teams
        boolean isCaptain = playerTeams.stream()
                .anyMatch(roster -> roster.getRoleInTeam() != null && roster.getRoleInTeam().name().equals("CAPTAIN"));
        
        model.addAttribute("isCaptain", isCaptain);

        return "player/playerProfile";
    }

    @PostMapping("/update-profileImage")
    public ResponseEntity<?> updateProfileImage(
        @RequestParam("profileImage") MultipartFile file
    ) throws IOException{

        // get Current Logged In Player

        try{
            User user = securityUtils.getLoggedInUser();

            PlayerProfile player = playerService.getPlayerProfileByUser(user).orElse(new PlayerProfile());

            
            playerService.uploadProfileImage(player.getId(), file);

            return ResponseEntity.ok().body("Profile image updated successfully!");
        }
        catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to store file: " + e.getMessage());
        }
    }


    @PostMapping("/update-field")
    public ResponseEntity<?> updatePlayerField(
        @RequestParam("field") String field,
        @RequestParam("value") String value
    ) {
        try {
            User user = securityUtils.getLoggedInUser();
            PlayerProfile player = playerService.getPlayerProfileByUser(user)
                .orElseThrow(() -> new RuntimeException("Player profile not found"));

            // Update the specific field based on what was passed from the frontend
            switch (field) {
                case "sport":
                    player.setPrimarySport(value);
                    break;
                case "position":
                    player.setPlayerRole(value);
                    break;
                case "experience":
                    player.setSkillLevel(value);
                    break;
                case "jersey":
                    player.setJerseyNumber(value);
                    break;
                case "phone":
                    player.setPhoneNumber(value);
                    break;
                case "address":
                    player.setCity(value);
                    break;
                case "dob":
                    player.setDateOfBirth(java.time.LocalDate.parse(value));
                    break;
                default:
                    return ResponseEntity.badRequest().body("Invalid field name");
            }

            playerService.savePlayerProfile(player);
            return ResponseEntity.ok().body("Field updated successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
    @PostMapping("/upload-id-proof")
    public ResponseEntity<?> uploadIdProof(@RequestParam("idDocument") MultipartFile idDocument) {
        try {
            User user = securityUtils.getLoggedInUser();
            if (user == null) {
                return ResponseEntity.status(401).body("User not Authenticated");
            }

            if (idDocument.isEmpty()) {
                return ResponseEntity.badRequest().body("Please select a file.");
            }

            playerService.saveIdProof(user, idDocument); // Your service method to save the file
            return ResponseEntity.ok("ID proof uploaded successfully!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    

    // To checking that player submit it's Id Proof or nottt...
    @GetMapping("/profile/status-check")
    @ResponseBody
    public ResponseEntity<?> checkPlayerIdProofStatus() {
        User user = securityUtils.getLoggedInUser();
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("hasIdProof", false));
        }

        PlayerProfile profile = playerService.getPlayerProfileByUser(user).orElse(null);
        boolean hasProof = (profile != null && profile.getPlayerIdProof() != null && !profile.getPlayerIdProof().trim().isEmpty());

        return ResponseEntity.ok(Map.of("hasIdProof", hasProof));
    }

    @PostMapping("/update-id-proof")
    @ResponseBody
    public ResponseEntity<?> updateIdProof(
        @RequestParam("idType") String idType,
        @RequestParam("idNumber") String idNumber,
        @RequestParam("idDocument") MultipartFile idDocument
    ) {
        try {
            User user = securityUtils.getLoggedInUser();
            if (user == null) {
                return ResponseEntity.status(401).body(Map.of("success", false, "message", "User not Authenticated"));
            }

            PlayerProfile playerProfile = playerService.getPlayerProfileByUser(user).orElse(null);
            if (playerProfile == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Player profile not found."));
            }

            // 1. Update ID Type and Number fields
            if (idType != null && !idType.isEmpty()) {
                playerProfile.setIdType(idType);
            }
            if (idNumber != null && !idNumber.isEmpty()) {
                playerProfile.setIdNumber(idNumber);
            }

            // 2. Handle File Upload if a new file was provided
            if (idDocument != null && !idDocument.isEmpty()) {
                String docFileName = "id_doc_" + UUID.randomUUID().toString();
                String docFileUrl = imageService.uploadImage(idDocument, docFileName);
                
                playerProfile.setPlayerIdProof(docFileUrl);
            }

            playerService.savePlayerProfile(playerProfile);

            return ResponseEntity.ok(Map.of("success", true, "message", "ID proof updated successfully!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/{playerId}/details")
    public ResponseEntity<?> getPlayerDetailsForOrganizer(@PathVariable Long playerId){
        PlayerProfile profile = playerService.getPlayerProfileById(playerId);
        if (profile == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Player not found"));
        }
        return ResponseEntity.ok(profile);
    }
}
