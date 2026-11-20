package com.champions_Call.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.champions_Call.model.match.Match;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public interface MatchRepository extends JpaRepository<Match,Long>{

  boolean existsByTournamentId(Long tournamentId);

  List<Match> findByTournamentId(Long tournamentId);

  Match findFirstByVenueLocationAndMatchDateAndMatchTimeAndIdNot(
    String venueLocation, 
    LocalDate matchDate, 
    LocalTime matchTime, 
    Long id
  );


  @Query("SELECT m FROM Match m WHERE m.matchDate = :date AND m.matchTime BETWEEN :startTime AND :endTime")
  List<Match> findMatchesByDateAndTimeRange(
      @Param("date") LocalDate date, 
      @Param("startTime") LocalTime startTime, 
      @Param("endTime") LocalTime endTime
  );

  // Grabs every single match in the database, ignoring dates/times completely for testing
    @Query("SELECT m FROM Match m")
    List<Match> findAllMatches();


    @Query("""
       SELECT m FROM Match m
        JOIN FETCH m.teamA
        JOIN FETCH m.teamB
        WHERE m.tournament.id = :tournamentId
        ORDER BY
          CASE m.status
            WHEN 'LIVE' THEN 0
            WHEN 'SCHEDULED' THEN 1
            WHEN 'COMPLETED' THEN 2
            ELSE 3
          END,
          m.matchTime ASC
        """)
    List<Match> findAllByTournamentOrderedByStatusPriority(@Param("tournamentId") Long tournamentId);



}
