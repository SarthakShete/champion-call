package com.champions_Call.service.managing_Scoring_services;

import java.util.List;

import com.champions_Call.model.Manging_Scoring.Cricket.CricketMatch;
import com.champions_Call.model.Manging_Scoring.Football.FootballMatch;

public interface FootballMatchService {

  FootballMatch save(FootballMatch FootballMatch);
  List<FootballMatch> saveAll(List<FootballMatch> FootballMatch);

}
