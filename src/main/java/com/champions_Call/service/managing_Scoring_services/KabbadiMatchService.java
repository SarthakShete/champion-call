package com.champions_Call.service.managing_Scoring_services;

import java.util.List;

import com.champions_Call.model.Manging_Scoring.Kabbadi.KabbadiMatch;

public interface KabbadiMatchService {

  KabbadiMatch save(KabbadiMatch kabbadiMatch);

  List<KabbadiMatch> saveAll(List<KabbadiMatch> kabbadiMatch);

}
