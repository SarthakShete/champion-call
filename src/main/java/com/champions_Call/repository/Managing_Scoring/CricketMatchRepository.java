package com.champions_Call.repository.Managing_Scoring;

import com.champions_Call.model.Manging_Scoring.Cricket.CricketBallEvent;
import com.champions_Call.model.Manging_Scoring.Cricket.CricketMatch;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository 
public interface CricketMatchRepository extends JpaRepository<CricketMatch
,Long>{

  

}
