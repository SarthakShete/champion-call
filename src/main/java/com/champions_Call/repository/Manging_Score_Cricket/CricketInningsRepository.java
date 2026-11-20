package com.champions_Call.repository.Manging_Score_Cricket;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.champions_Call.model.Manging_Scoring.Cricket.CricketInnings;

public interface CricketInningsRepository extends JpaRepository<CricketInnings,Long>{

  // findByCricketMatch_IdAndInningsNumber() — finds a specific innings of a cricket match.
  // findByCricketMatch_IdOrderByInningsNumberAsc() — gets all innings of a match in order (1st, 2nd, etc.).

    Optional<CricketInnings> findByCricketMatch_IdAndInningsNumber(Long cricketMatchId, int inningsNumber);

    List<CricketInnings> findByCricketMatch_IdOrderByInningsNumberAsc(Long cricketMatchId);

}
