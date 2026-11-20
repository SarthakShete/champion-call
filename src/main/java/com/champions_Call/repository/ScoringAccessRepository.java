package com.champions_Call.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import com.champions_Call.model.match.ScoringAccess;


public interface ScoringAccessRepository extends JpaRepository<ScoringAccess,Long>{

  // NEW: used when the scorer opens the email link
  // Finds the ScoringAccess row that has this token from the link
  Optional<ScoringAccess> findByAccessToken(String accessToken);

  // NEW: used on every scoring API call, to check "is this scorer verified?"
  //Finds the row for the verified scorer, so we can check status, match and expiry
  Optional<ScoringAccess> findBySessionToken(String sessionToken);


  Optional<ScoringAccess> findTopByMatch_IdOrderByCreatedAtDesc(Long matchId);


  // Get scorer assignments for a tournament, newest first
  @Query("""
    SELECT s
    FROM ScoringAccess s
    WHERE s.match.tournament.id = :tournamentId
      AND s.createdAt = (
          SELECT MAX(s2.createdAt)
          FROM ScoringAccess s2
          WHERE s2.match.id = s.match.id
      )
    ORDER BY s.createdAt DESC
    """)
  List<ScoringAccess> findLatestAssignmentsByTournament(
          @Param("tournamentId") Long tournamentId
  );

  Optional<ScoringAccess> findTopByMatch_IdOrderByCreatedAtDescIdDesc(
        Long matchId);

}
