package com.champions_Call.service.managing_Scoring_services;

import java.util.List;

import com.champions_Call.model.Manging_Scoring.Cricket.CricketMatch;

public interface CricketMatchService {

  CricketMatch save(CricketMatch cricketMatch);

  List<CricketMatch> saveAll(List<CricketMatch> cricketMatches);
}
