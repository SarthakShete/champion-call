package com.champions_Call.repository.Manging_Score_Cricket;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.champions_Call.model.Manging_Scoring.Cricket.CricketPlayerStats;

public interface CricketPlayerStatsRepository extends JpaRepository<CricketPlayerStats,Long>{


  // findByInnings_IdAndPlayerId() — finds a player's batting and bowling stats for a particular innings.

  // findByInnings_Id() — retrieves all player stats for an innings.
  
  Optional<CricketPlayerStats> findByInnings_IdAndPlayer_Id(
        Long inningsId,
        Long playerId
  );

  List<CricketPlayerStats> findByInnings_Id(Long inningsId);

}
