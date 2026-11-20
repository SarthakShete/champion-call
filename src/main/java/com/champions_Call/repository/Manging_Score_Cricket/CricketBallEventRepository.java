package com.champions_Call.repository.Manging_Score_Cricket;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.champions_Call.model.Manging_Scoring.Cricket.CricketBallEvent;

public interface CricketBallEventRepository extends JpaRepository<CricketBallEvent,Long>{


  // findByEventId() — finds a scoring event using its unique event ID.
  // findByInnings_IdOrderByEventSequenceAsc() — retrieves all ball events of an innings in chronological order.

  Optional<CricketBallEvent> findByEventId(String eventId);

  List<CricketBallEvent> findByInnings_IdOrderByEventSequenceAsc(Long inningsId);
}
