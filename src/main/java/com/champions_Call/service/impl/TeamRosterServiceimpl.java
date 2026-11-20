package com.champions_Call.service.impl;

import java.util.List;

import com.champions_Call.Helper.TournamentRole;
import com.champions_Call.model.Team.TeamRegistration;
import com.champions_Call.model.Team.TeamRoster;
import com.champions_Call.model.player.PlayerProfile;
import com.champions_Call.repository.TeamRosterRepository;
import com.champions_Call.service.TeamRosterService;
import com.champions_Call.service.PlayerService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.champions_Call.model.User;

@Service
public class TeamRosterServiceimpl implements TeamRosterService {

    @Autowired
    private TeamRosterRepository teamRosterRepository;

    @Autowired
    private PlayerService playerService;

    @Override
    public TeamRoster saveTeamRoster(TeamRoster roster) {
        return teamRosterRepository.save(roster);
    }
    
    @Override
    public List<TeamRoster> getTeamsForPlayer(PlayerProfile playerProfile) {
        return teamRosterRepository.findByPlayerProfile(playerProfile);
    }

    public boolean isUserCaptainOfTeam(User user, TeamRegistration teamRegistration) {

        PlayerProfile player = playerService.getPlayerProfileByUser(user).orElse(null);
        if (player == null) return false;

        TeamRoster roster = teamRosterRepository.findByTeamRegistrationAndPlayerProfile(teamRegistration, player);
        return roster != null && roster.getRoleInTeam() == TournamentRole.CAPTAIN;
    }

    public List<TeamRoster> getByTeamRegistrationId(Long teamRegId) {
        // Assuming you are using a Spring Data JPA repository:
        return teamRosterRepository.findByTeamRegistrationId(teamRegId);
    }
}