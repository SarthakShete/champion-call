package com.champions_Call.service.impl;

import java.time.LocalDate;
import java.util.Comparator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.champions_Call.Helper.PlayerStatus;
import com.champions_Call.Helper.RegistrationTeamStatus;
import com.champions_Call.Helper.RosterRole;
import com.champions_Call.Helper.TournamentRole;
import com.champions_Call.model.User;
import com.champions_Call.model.Team.TeamRegistration;
import com.champions_Call.model.Team.TeamRoster;
import com.champions_Call.model.Tournament.Tournament;
import com.champions_Call.model.Tournament.TournamentStatus;
import com.champions_Call.model.player.PlayerProfile;
import com.champions_Call.repository.TeamRegistrationRepository;
import com.champions_Call.service.*;

import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

import com.champions_Call.repository.TeamRosterRepository;
import com.champions_Call.repository.UserRepo;

@Service
public class TeamRegistrationServiceImpl implements TeamRegistrationService{

  @Autowired
  private TeamRegistrationRepository teamRegistrationRepo;

  @Autowired
  private UserServices userServices;

  @Autowired
  private PlayerService playerService;

  @Autowired
  private TeamRosterRepository teamRosterRepository;


    @Override
    public TeamRegistration saveTeam(TeamRegistration team) {
       return teamRegistrationRepo.save(team);
    }

    @Override
    public boolean  hasUserRegisteredForTournament(Tournament tournament,User user){
      return teamRegistrationRepo.existsByTournamentAndRegisteredBy (tournament,user);
    }

    @Override
    public List<TeamRegistration> getTeamsByTournament(Tournament tournament){
        return teamRegistrationRepo.findByTournament(tournament);
    }

    @Override
    public Optional<TeamRegistration> getTeamRegistrationById(Long id) {
        return teamRegistrationRepo.findById(id);
    }

    @Override
    public TeamRegistration getTeamById(Long id){
        return teamRegistrationRepo.findById(id).orElse(null);
    }

    @Override
    public List<TeamRegistration> getTeamsByTournamentId(Long tournamentId){
        return teamRegistrationRepo.findByTournamentId(tournamentId);
    }

    
    @Transactional
    public void joinTeamWithCode(String email,Long teamRegistrationId,String inviteCode){
      
        User user = userServices.getUserByEmail(email);

        if (user == null) {
          throw new RuntimeException("User not found");
        }
        
        
        PlayerProfile player = playerService.getPlayerProfileByUser(user)
              .orElseThrow(() -> new RuntimeException("Player profile not found. Please complete profile setup first."));

        TeamRegistration teamReg = teamRegistrationRepo.findById(teamRegistrationId)
        .orElseThrow(() -> new RuntimeException("Team registration not found"));

        if(teamReg.getInviteCode() == null || !teamReg.getInviteCode().equalsIgnoreCase(inviteCode.trim())){
          throw new RuntimeException("Invalid invite code. Please check with your team captain.");
        }
        
        Tournament currentTournament = teamReg.getTournament();

        // 2. Check if the player is ALREADY in ANY team registered for THIS tournament
        TeamRoster existingRoster = teamRosterRepository.findByTeamRegistration_TournamentAndPlayerProfile(currentTournament, player);

        if (existingRoster != null) {
            String existingTeamName = existingRoster.getTeamRegistration().getTeamName(); // Adjust getter if your team name field is named differently
            throw new RuntimeException("You are already playing for team '" + existingTeamName + "' in this tournament and cannot join another team!");
        }

        // 6. Create and save the new TeamRoster entry
        TeamRoster newMember = new TeamRoster();
        newMember.setTeamRegistration(teamReg);
        newMember.setPlayerProfile(player);
        newMember.setRoleInTeam(TournamentRole.PLAYER); // Default role for joined members

        teamRosterRepository.save(newMember);


        LocalDate currentDate = LocalDate.now();
        LocalDate startDate = teamReg.getTournament().getStartDate();
        LocalDate endDate = teamReg.getTournament().getEndDate();

        if(startDate != null && endDate != null &&
          !currentDate.isBefore(startDate) && !currentDate.isAfter(endDate)
        ){
          player.setPlayerStatus(PlayerStatus.PLAYING);
        }
        else{
          player.setPlayerStatus(PlayerStatus.RESERVED);
        }
        playerService.savePlayerProfile(player);
    }

    @Transactional
    public void releasePlayersAfterTournamentOrElimination(Long teamRegistrationId){

      TeamRegistration teamReg = teamRegistrationRepo.findById(teamRegistrationId)
            .orElseThrow(() -> new RuntimeException("Team registration not found"));

      // fetch all team Memebers of these current Team
      List<TeamRoster> members = teamRosterRepository.findByTeamRegistration(teamReg);

      for (TeamRoster member : members) {
        PlayerProfile player = member.getPlayerProfile();
        
        // Check if the player is still active in any other ongoing tournament
        boolean hasOtherActiveTournaments = teamRosterRepository.existsByPlayerProfileAndTeamRegistration_Tournament_Status(
            player, TournamentStatus.ONGOING
        );

        // Only set them back to AVAILABLE if they have NO other active tournaments
        if (!hasOtherActiveTournaments) {
            player.setPlayerStatus(PlayerStatus.AVAILABLE);
            playerService.savePlayerProfile(player);
        }
    }
  }

  @Transactional
  public void processTeamAction(Long teamRegId,Long rosterId,String action){

    // fetch the specific team 
    TeamRegistration teamReg = teamRegistrationRepo.findById(teamRegId)
    .orElseThrow(() -> new RuntimeException("Team registration not found with id: " + teamRegId));

    // fetch the target player...
    TeamRoster targetMember = teamRosterRepository.findById(rosterId)
        .orElseThrow(() -> new RuntimeException("Team Member Not found"));
      
    // verify that targetMember is actually belongs to the team

    if(!targetMember.getTeamRegistration().getId().equals(teamReg.getId())){
      throw new RuntimeException("This member does not belong to the specified team ! ");
    }

    switch(action){

        case "ASSIGN_VICE_CAPTAIN":
            // 1. Find the current Vice-Captain (if any) and demote them to PLAYER
            TeamRoster currentViceCaptain = teamRosterRepository.findByTeamRegistrationAndRoleInTeam(teamReg, TournamentRole.VICE_CAPTAIN)
            .orElse(null);

            if(currentViceCaptain != null){
                currentViceCaptain.setRoleInTeam(TournamentRole.PLAYER);
                teamRosterRepository.save(currentViceCaptain);
            }

            // 2. Promote the target member to VICE_CAPTAIN
            targetMember.setRoleInTeam(TournamentRole.VICE_CAPTAIN);
            teamRosterRepository.save(targetMember);
            break;
        
        case "REMOVE_VICE_CAPTAIN":
            targetMember.setRoleInTeam(TournamentRole.PLAYER);
            teamRosterRepository.save(targetMember);
            break;

        case "TRANSFER_CAPTAINCY":

            // 1. find the current Captain
            TeamRoster currentCaptain = teamRosterRepository.findByTeamRegistrationAndRoleInTeam(teamReg,TournamentRole.CAPTAIN)
            .orElse(null);

            if(currentCaptain != null){
              // make a current captain Player...
               currentCaptain.setRoleInTeam(TournamentRole.PLAYER);
               teamRosterRepository.save(currentCaptain);
            }
            
            // 2. Promote the target member to CAPTAIN
            targetMember.setRoleInTeam(TournamentRole.CAPTAIN);
            teamRosterRepository.save(targetMember);

            break;

        case "REMOVE_PLAYER":
            teamRosterRepository.delete(targetMember);
            break;

        default:
            throw new RuntimeException("Invalid action command provided.");
    }
  }


  public List<TeamRoster> getSortedTeamSquad(Long teamRegId) {
        TeamRegistration teamReg = teamRegistrationRepo.findById(teamRegId)
            .orElseThrow(() -> new RuntimeException("Team registration not found with id: " + teamRegId));

        List<TeamRoster> squad = teamRosterRepository.findByTeamRegistration(teamReg);

        // Sort squad: Captain = 1st, Vice-Captain = 2nd, Regular Player = 3rd
        squad.sort(Comparator.comparingInt(member -> {
            if (member.getRoleInTeam() == TournamentRole.CAPTAIN) return 1;
            if (member.getRoleInTeam() == TournamentRole.VICE_CAPTAIN) return 2;
            return 3;
        }));

        return squad;
    }


    @Override
    @Transactional
    public List<TeamRegistration> getTeamsByPlayer(PlayerProfile player) {
        // Guard check if the player or user is null
        if (player == null || player.getUser() == null) {
            return new java.util.ArrayList<>();
        }
        
        // Query team registrations using the User linked inside the PlayerProfile
        return teamRegistrationRepo.findByRegisteredBy(player.getUser());
    }
    
    public List<TeamRegistration> getTournamentByIdAndStatus(Long tournamentId, RegistrationTeamStatus status){

        return teamRegistrationRepo.findByTournamentIdAndStatus(tournamentId, status);
    }
    
}
