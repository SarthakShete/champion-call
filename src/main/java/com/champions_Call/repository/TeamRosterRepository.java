package com.champions_Call.repository;

import com.champions_Call.model.Team.*;
import com.champions_Call.model.Tournament.Tournament;
import com.champions_Call.model.Tournament.TournamentStatus;

import java.util.List;
import java.util.Optional;
import javax.swing.Spring;

import org.antlr.v4.runtime.atn.SemanticContext.AND;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.champions_Call.model.player.PlayerProfile;
import com.champions_Call.Helper.TournamentRole;

public interface TeamRosterRepository extends JpaRepository<TeamRoster,Long>{
   
   // find all team member where the player is captain or vice-captain
  //  Behind the scenes, Spring generates a query equivalent to: SELECT * FROM team_roster WHERE player_profile_id = ? AND role_in_team IN (?, ?)

  // JPQL
  // @Query("SELECT tr FROM TeamRoster tr WHERE tr.playerProfile = :playerProfile AND tr.roleInTeam IN :roles")
  //   List<TeamRoster> findByPlayerProfileAndRoleInTeamIn(
  //       @Param("playerProfile") PlayerProfile playerProfile, 
  //       @Param("roles") List<TournamentRole> roles
  //   );
  
   List<TeamRoster> findByPlayerProfileAndRoleInTeamIn(
     PlayerProfile playerProfile,
     List<TournamentRole> roles
   );


 TeamRoster findByTeamRegistration_TournamentAndPlayerProfile(Tournament tournament, PlayerProfile playerProfile);


 List<TeamRoster> findByPlayerProfile(PlayerProfile playerProfile);

 List<TeamRoster> findByTeamRegistration(TeamRegistration teamRegistration);

   // Checks if this player is in any other team roster where the tournament status is still active/ongoing
   boolean existsByPlayerProfileAndTeamRegistration_Tournament_Status(PlayerProfile playerProfile, TournamentStatus status);


   TeamRoster findByTeamRegistrationAndPlayerProfile(TeamRegistration teamRegistration, PlayerProfile playerProfile);

   Optional<TeamRoster> findByTeamRegistrationAndRoleInTeam(TeamRegistration teamRegistration, TournamentRole roleInTeam);

   Optional<TeamRoster> findById(Long rosterId);

    @Query("SELECT r FROM TeamRoster r JOIN FETCH r.playerProfile WHERE r.teamRegistration.id = :teamRegId")
    List<TeamRoster> findByTeamRegistrationId(@Param("teamRegId") Long teamRegId);
}