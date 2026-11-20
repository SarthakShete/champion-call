package com.champions_Call.service.impl;


import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

import jakarta.persistence.criteria.Join;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.champions_Call.model.Tournament.Tournament;
import com.champions_Call.model.Tournament.TournamentRule;
import com.champions_Call.model.Tournament.TournamentStatus;
import com.champions_Call.model.match.ManualFixturesRequestDto;
import com.champions_Call.model.match.Match;
import com.champions_Call.model.organizer.OrganizerProfile;
import com.champions_Call.model.organizer.Prize;
import com.champions_Call.model.player.PlayerProfile;
import com.champions_Call.repository.TournamentRepository;
import com.champions_Call.repository.MatchRepository;
import com.champions_Call.repository.OrganizerProfileRepo;
import com.champions_Call.service.TournamentService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import com.champions_Call.Helper.RegistrationTeamStatus;
import com.champions_Call.model.Team.TeamRegistration;
import com.champions_Call.service.MatchService;
import com.champions_Call.service.TeamRegistrationService;

import jakarta.persistence.criteria.Predicate;
import com.champions_Call.Helper.MatchStatus;


@Service
public class TournamentServiceImpl implements TournamentService{

  @Autowired
  private TournamentRepository tournamentRepo;

  @Autowired
  private OrganizerProfileRepo organizerProfileRepo;

  @Autowired
  private TeamRegistrationService teamRegistrationService;

  @Autowired
  private MatchService matchService;

  @Override
  public Tournament getTournamentId(Long id) {
    
    Optional<Tournament> tournament =  tournamentRepo.findById(id);

    return tournament.orElse(null);
  }
   
  
  @Override
  public boolean isTournamentNameExistAndIdNot(String tournamentName,Long id){
    return tournamentRepo.existsByTournamentNameAndIdNot(tournamentName,id);
  }


  @Override
  public boolean isTournamentNameExist(String tournamentName){
    return tournamentRepo.existsByTournamentName(tournamentName);
  }

  @Override
    @Transactional
    public Tournament saveTournament(Tournament tournament) {
        System.out.println("The saveTournament is called....");

        // Just save the aggregate root. If Tournament.customRules and Tournament.prizes
        // are mapped with cascade = CascadeType.ALL (or PERSIST/MERGE) and
        // orphanRemoval = true, Hibernate will automatically insert/update/delete
        // the child rows to match whatever is in tournament.getCustomRules() /
        // tournament.getPrizes() at this point — no manual SQL needed.
        Tournament savedTournament = tournamentRepo.save(tournament);

        System.out.println("The Tournament is saved. Rule count: " +
            (savedTournament.getCustomRules() != null ? savedTournament.getCustomRules().size() : 0) +
            ", Prize count: " +
            (savedTournament.getPrizes() != null ? savedTournament.getPrizes().size() : 0));

        return savedTournament;
    }
    public List<Tournament> getAllTournaments(){
        return tournamentRepo.findAll();
    }

    @Override
    public List<Tournament> getTournamentsByOrganizer(Long id) {
      
      List<Tournament> all = tournamentRepo.findByOrganizerId(id);

      return all.stream()
              .filter(t -> t.getTournamentName() != null && t.getSport() != null)
              .collect(Collectors.toList());
    }

    @Override
    public List<Tournament> searchByFilters(Long organizerId, String keyword, String sport, String status) {
        // 1. Log current state
        System.out.println("DEBUG: Searching for OrgID: " + organizerId + 
                            ", Keyword: '" + keyword + "', Sport: '" + sport + "', Status: '" + status + "'");

        // 2. Define the Specification
        Specification<Tournament> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // Ensure we only search for tournaments belonging to this organizer
            // We use a Join to safely access the organizer's ID
            Join<Tournament, OrganizerProfile> organizerJoin = root.join("organizer");
            predicates.add(cb.equal(organizerJoin.get("id"), organizerId));

            // Keyword filter: search in name OR location (OR logic)
            if (keyword != null && !keyword.trim().isEmpty()) {
                String pattern = "%" + keyword.toLowerCase().trim() + "%";
                Predicate nameMatch = cb.like(cb.lower(root.get("tournamentName")), pattern);
                Predicate locationMatch = cb.like(cb.lower(root.get("venueLocation")), pattern);
                predicates.add(cb.or(nameMatch, locationMatch));
            }
            
            // Sport filter
            if (sport != null && !sport.isEmpty()) {
                predicates.add(cb.equal(root.get("sport"), sport));
            }

            // Status filter
            if (status != null && !status.isEmpty() && !status.equals("ALL")) {
                try {
                    TournamentStatus statusEnum = TournamentStatus.valueOf(status.toUpperCase());
                    predicates.add(cb.equal(root.get("status"), statusEnum));
                } catch (IllegalArgumentException e) {
                    System.err.println("DEBUG: Invalid status provided: " + status);
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // 3. Execute
        List<Tournament> filtered = tournamentRepo.findAll(spec);
        System.out.println("DEBUG: Database returned " + filtered.size() + " rows after filtering.");
        
        return filtered;
    }

    @Override
    public boolean canPublish(int OrganizerId) {
        
      Optional<OrganizerProfile> organizerProfile = organizerProfileRepo.findByUserId(OrganizerId);
    
      if(organizerProfile == null) return false;

      OrganizerProfile profile = organizerProfile.get();

      return profile.isIdentityVerified();
       
    }

    @Override
    public Page<Tournament> findAll(Specification<Tournament> spec, Pageable pageable){
        return tournamentRepo.findAll(spec,pageable);
    }

    public List<Tournament> getTournamentsForPlayer(PlayerProfile player) {
        // Query your repository to find tournaments where the player is registered or part of a team
        return tournamentRepo.findTournamentsByPlayer(player);
    }

    public void generateFixturesForTournament(Long tournamentId){

        Tournament tournament = tournamentRepo.findById(tournamentId)
        .orElseThrow(() -> new RuntimeException("Tournament Not Foundd"));

       


        // get only approved teams

        List<TeamRegistration> approvedTeams = teamRegistrationService.getTournamentByIdAndStatus(tournamentId,RegistrationTeamStatus.APPROVED);

        if(approvedTeams.size() < 2){
            throw new RuntimeException("You need at least 2 approved teams to generate fixtures.");
        }

        // get a format of Tournament
        String style = tournament.getTournamentStyle();

        if ("knockout".equalsIgnoreCase(style)) {
            generateKnockoutFixtures(tournament, approvedTeams);
        } 
        else if ("league".equalsIgnoreCase(style)) {
            generateLeagueFixtures(tournament, approvedTeams);
        }       
        else if ("group_stage_knockout".equalsIgnoreCase(style) || "group stage".equalsIgnoreCase(style)) {
            generateGroupStageAndKnockoutFixtures(tournament, approvedTeams);
        } 
        else {
            throw new RuntimeException("Invalid or unsupported tournament style specified: " + style);
        }
        tournament.setFixturesGenerated(true);
        tournamentRepo.save(tournament);
    }

    private void generateKnockoutFixtures(Tournament tournament,List<TeamRegistration> approvedTeams){

        // create a new List of approved so later we can modify it
        List<TeamRegistration> teamsToPair = new ArrayList<>(approvedTeams);

        // randomly shuffle the team
        Collections.shuffle(teamsToPair);

        int totalTeams = teamsToPair.size();
        Integer roundNumbers = 1;

        List<Match> fixturesToSave = new ArrayList<>();

        for(int i=0;i<totalTeams;i+=2){

            // grab the first team in the pair
            TeamRegistration teamA = teamsToPair.get(i);

            // Grab the second team, IF they exist. 
            // If 'i+1' is out of bounds, teamB becomes null (happens with odd number of teams)
            TeamRegistration teamB = (i+1 < totalTeams) ? teamsToPair.get(i+1) : null;

            Match fixture = new Match();
            fixture.setTournament(tournament);
            fixture.setRoundNumber("round"+roundNumbers);
            fixture.setTeamA(teamA);
            fixture.setMatchDate(LocalDate.now().plusDays(1 + (i / 2))); // Starts tomorrow
            fixture.setMatchTime(LocalTime.of(18, 0));

            // 5. Assign status using the MatchStatus Enum
            if (teamB != null) {
                fixture.setTeamB(teamB);
                fixture.setStatus(MatchStatus.SCHEDULED); // Normal match
            } else {
                fixture.setTeamB(null);
                fixture.setStatus(MatchStatus.BYE); // Odd team out gets a free pass to Round 2
            }

            fixturesToSave.add(fixture);
        }

        matchService.saveAll(fixturesToSave);
    }

    private void generateLeagueFixtures(Tournament tournament, List<TeamRegistration> approvedTeams){
    
        List<TeamRegistration> teams = new ArrayList<>(approvedTeams);
        int totalTeams = teams.size();

        // If odd number of teams, add a dummy/null "BYE" entry to balance the circle method
        boolean hasBye = false;
        if (totalTeams % 2 != 0) {
            teams.add(null); 
            totalTeams++;
            hasBye = true;
        }

        int totalRounds = totalTeams - 1;
        int matchesPerRound = totalTeams / 2;
        List<Match> fixturesToSave = new ArrayList<>();

        // Loop through each round
        for (int round = 1; round <= totalRounds; round++) {
            int roundNumber = round;

            // Pair teams for the current round
            for (int i = 0; i < matchesPerRound; i++) {
                TeamRegistration homeTeam = teams.get(i);
                TeamRegistration awayTeam = teams.get(totalTeams - 1 - i);

                // If one of the slots is the dummy BYE team, handle it or skip
                if (homeTeam == null || awayTeam == null) {
                    // This means the non-null team gets a bye week for this round
                    continue; 
                }

                // Create the Match entity
                Match fixture = new Match();
                fixture.setTournament(tournament);
                fixture.setRoundNumber("round" +roundNumber);
                fixture.setTeamA(homeTeam);
                fixture.setTeamB(awayTeam);
                fixture.setStatus(MatchStatus.SCHEDULED);
                fixture.setMatchDate(LocalDate.now().plusDays(round)); // Increment day per round
                fixture.setMatchTime(LocalTime.of(18, 0));

                fixturesToSave.add(fixture);

                // Save the league match to your database
                // matchRepository.save(fixture);
            }

            // Rotate the teams in the circle (keeping index 0 fixed)
            TeamRegistration lastTeam = teams.remove(teams.size() - 1);
            teams.add(1, lastTeam);
        }
        matchService.saveAll(fixturesToSave);
    }

    private void generateGroupStageAndKnockoutFixtures(Tournament tournament, List<TeamRegistration> approvedTeams) {

        List<TeamRegistration> teamsToGroup = new ArrayList<>(approvedTeams);
        
        // 1. Shuffle teams randomly to ensure fair distribution across groups
        Collections.shuffle(teamsToGroup);
        
        int totalTeams = teamsToGroup.size();
        
        // Safety check: You need at least 4 teams to make a meaningful group stage
        if (totalTeams < 4) {
            throw new IllegalArgumentException("At least 4 teams are required for Group Stage + Knockout format.");
        }

        // 2. Determine how many groups to create (e.g., 4 teams per group standard)
        // For simplicity, let's divide teams into 2 main groups: Group A and Group B
        int numberOfGroups = (totalTeams >= 8) ? 4 : 2; 
        
        List<List<TeamRegistration>> groups = new ArrayList<>();
        for (int g = 0; g < numberOfGroups; g++) {
            groups.add(new ArrayList<>());
        }

        // 3. Distribute teams evenly into the groups (Snake/Round-robin distribution)
        for (int i = 0; i < totalTeams; i++) {
            int groupIndex = i % numberOfGroups;
            groups.get(groupIndex).add(teamsToGroup.get(i));
        }

        // 4. Generate Round-Robin fixtures *inside* each group
        char groupNameChar = 'A';
        for (List<TeamRegistration> groupTeams : groups) {
            String groupName = "Group " + groupNameChar;
            
            // Run a mini round-robin for the teams inside this specific group
            generateRoundRobinForGroup(tournament, groupTeams, groupName);
            
            groupNameChar++;
        }
        
        // Note: The Knockout stage matches (Semi-finals/Finals) are dynamically generated 
        // *after* group stage matches are played and points tables are calculated.
    }

    // Helper method to generate matches within a specific group
    private void generateRoundRobinForGroup(Tournament tournament,              List<TeamRegistration> groupTeams, String groupName) {
        
        int size = groupTeams.size();
        
        // If odd number in a group, add a null placeholder for BYE
        if (size % 2 != 0) {
            groupTeams.add(null);
            size++;
        }

        int totalRounds = size - 1;
        int matchesPerRound = size / 2;

        List<Match> groupFixturesToSave = new ArrayList<>();

        for (int round = 1; round <= totalRounds; round++) {
            for (int i = 0; i < matchesPerRound; i++) {
                TeamRegistration home = groupTeams.get(i);
                TeamRegistration away = groupTeams.get(size - 1 - i);

                if (home == null || away == null) {
                    continue; // Bye week for this group member
                }

                // Create match entity
                Match fixture = new Match();
                fixture.setTournament(tournament);
                fixture.setRoundNumber("Round " + round);
                // Result: "Round 1", "Round 2", etc.
                fixture.setTeamA(home);
                fixture.setTeamB(away);
                fixture.setStatus(MatchStatus.SCHEDULED);
                fixture.setMatchDate(LocalDate.now().plusDays(round));
                fixture.setMatchTime(LocalTime.of(17, 0));

                groupFixturesToSave.add(fixture);
                
                // Optional: If your Match entity has a group field, you can set it here:
                // fixture.setGroupName(groupName);

                // Save match to database
                // matchRepository.save(fixture);
            }

            // Rotate circle
            TeamRegistration last = groupTeams.remove(groupTeams.size() - 1);
            groupTeams.add(1, last);
        }
        if (!groupFixturesToSave.isEmpty()) {
            matchService.saveAll(groupFixturesToSave);
        }
    }

    @Override
    public void saveManualFixturesBatch(Long tournamentId, ManualFixturesRequestDto requestDto) {

        Tournament tournament = tournamentRepo.findById(tournamentId)
            .orElseThrow(() -> new RuntimeException("Tournament Not Found"));

        List<Match> matchesToSave = new ArrayList<>();
        
        for (ManualFixturesRequestDto.ManualFixtureItemDto dto : requestDto.getFixtures()) {
                    Match match = new Match();
                    match.setTournament(tournament);
                    match.setRoundNumber(dto.getRoundNumber());
                if (dto.getMatchDate() != null && !dto.getMatchDate().isEmpty()) {  
                match.setMatchDate(LocalDate.parse(dto.getMatchDate()));
            }
            
            // FIX: Parse String time to LocalTime (handle "HH:mm" or "HH:mm:ss")
            if (dto.getMatchTime() != null && !dto.getMatchTime().isEmpty()) {
                String timeStr = dto.getMatchTime();
                if (timeStr.length() == 5) { // "18:00" -> append seconds for parsing
                    timeStr += ":00";
                }
                match.setMatchTime(LocalTime.parse(timeStr));
            }
            match.setVenueLocation(dto.getVenueLocation());
            match.setStatus(MatchStatus.SCHEDULED);

            // Look up and assign Team A using the ID from JavaScript
            if (dto.getTeamAId() != null) {
                TeamRegistration teamA = teamRegistrationService.getTeamById(dto.getTeamAId());
                match.setTeamA(teamA);
            }

            // Look up and assign Team B using the ID from JavaScript
            if (dto.getTeamBId() != null) {
                TeamRegistration teamB = teamRegistrationService.getTeamById(dto.getTeamBId());
                match.setTeamB(teamB);
            }

            matchesToSave.add(match);
        }
        
        // Save all matches to the database
        matchService.saveAll(matchesToSave);

        // Update the tournament's generated flag so it reflects on your dashboard UI
        tournament.setFixturesGenerated(true);
        tournamentRepo.save(tournament);
    }
}
