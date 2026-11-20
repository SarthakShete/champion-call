package com.champions_Call.service;

import com.champions_Call.Helper.RegistrationTeamStatus;
import com.champions_Call.model.*;
import com.champions_Call.model.Team.TeamRegistration;
import com.champions_Call.model.Team.TeamRoster;
import com.champions_Call.model.Tournament.Tournament;
import com.champions_Call.model.player.PlayerProfile;

import java.util.List;
import java.util.Optional;

public interface TeamRegistrationService {
     

  TeamRegistration saveTeam(TeamRegistration team);

  boolean hasUserRegisteredForTournament(Tournament tournament,User user);

  List<TeamRegistration> getTeamsByTournament(Tournament tournament);

  List<TeamRegistration> getTeamsByTournamentId(Long tournamentId);

  TeamRegistration getTeamById(Long id);

  Optional<TeamRegistration> getTeamRegistrationById(Long id);

  void joinTeamWithCode(String email,Long teamRegistrationId,String inviteCode);

  void processTeamAction(Long teamRegId,Long rosterId,String action);


  List<TeamRoster> getSortedTeamSquad(Long teamRegId);


  List<TeamRegistration> getTeamsByPlayer(PlayerProfile player);

  List<TeamRegistration> getTournamentByIdAndStatus(Long tournamentId, RegistrationTeamStatus status);

}
