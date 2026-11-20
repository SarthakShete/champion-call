package com.champions_Call.service;

import java.util.List;

import com.champions_Call.model.Team.*;
import com.champions_Call.model.player.PlayerProfile;


public interface TeamRosterService {

  TeamRoster saveTeamRoster(TeamRoster team);

  List<TeamRoster> getTeamsForPlayer(PlayerProfile playerProfile);

  List<TeamRoster> getByTeamRegistrationId(Long teamRegId);

}
