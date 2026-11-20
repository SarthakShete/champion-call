package com.champions_Call.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.champions_Call.model.*;
import com.champions_Call.model.Tournament.Tournament;
import com.champions_Call.model.player.PlayerProfile;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

@Repository
public  interface  TournamentRepository extends JpaRepository<Tournament,Long>, JpaSpecificationExecutor<Tournament>{

  Optional<Tournament> findById(Long id);

  boolean existsByTournamentNameAndIdNot(String tournamentName,Long id);

  boolean existsByTournamentName(String tournamentName);

  List<Tournament> findByOrganizerId(Long id);


  @Query("SELECT t FROM Tournament t WHERE t.status NOT IN ('COMPLETED', 'CANCELLED')")
  List<Tournament> findActiveTournaments();


  @Query("SELECT t FROM Tournament t WHERE " +
           "LOWER(t.tournamentName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(t.venueLocation) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(t.sport) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(t.status) LIKE LOWER(CONCAT('%', :keyword, '%'))")
  List<Tournament> searchAllFields(@Param("keyword") String keyword);


  @Modifying
  @Query("DELETE FROM TournamentRule r WHERE r.tournament.id = :tournamentId")
  void deleteRulesByTournamentId(@Param("tournamentId") Long tournamentId);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query(value = "INSERT INTO tournament_rules (description, rule_text, tournament_id) VALUES (:desc, :text, :tId)", nativeQuery = true)
  void insertRuleDirectly(@Param("desc") String description, @Param("text") String ruleText, @Param("tId") Long tournamentId);

   @Modifying(clearAutomatically = true, flushAutomatically = true)
@Query("DELETE FROM Prize p WHERE p.tournament.id = :tournamentId")
void deletePrizesByTournamentId(@Param("tournamentId") Long tournamentId);

@Modifying(clearAutomatically = true, flushAutomatically = true)
@Query(value = "INSERT INTO tournament_prizes (prize_name, prize_type, prize_details, tournament_id) VALUES (:name, :type, :details, :tId)", nativeQuery = true)
void insertPrizeDirectly(@Param("name") String prizeName, @Param("type") String prizeType, @Param("details") String prizeDetails, @Param("tId") Long tournamentId);


    @Query("SELECT DISTINCT tr.tournament FROM TeamRegistration tr " +
           "JOIN tr.roster roster " +
           "WHERE roster.playerProfile = :player")
    List<Tournament> findTournamentsByPlayer(@Param("player") PlayerProfile player);
   
} 
