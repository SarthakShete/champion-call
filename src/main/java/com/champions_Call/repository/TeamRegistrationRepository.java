package com.champions_Call.repository;

import com.champions_Call.Helper.RegistrationTeamStatus;
import com.champions_Call.model.User;
import com.champions_Call.model.Team.TeamRegistration;
import com.champions_Call.model.Tournament.Tournament;
import com.champions_Call.model.player.PlayerProfile;

import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeamRegistrationRepository extends JpaRepository<TeamRegistration,Long>{


  boolean existsByTournamentAndRegisteredBy(Tournament tournament,User registeredBy);


  List<TeamRegistration> findByTournament(Tournament tournament);

  Optional<TeamRegistration> findById(Long teamRegId);

  // public List<TeamRegistration> findByPlayer(PlayerProfile player);
  

  List<TeamRegistration> findByRegisteredBy(User registeredBy);

  List<TeamRegistration> findByTournamentIdAndStatus(Long tournamentId, RegistrationTeamStatus status);

  List<TeamRegistration> findByTournamentId(Long tournamentId);
}
