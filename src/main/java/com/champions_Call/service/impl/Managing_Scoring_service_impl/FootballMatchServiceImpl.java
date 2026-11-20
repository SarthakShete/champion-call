
package com.champions_Call.service.impl.Managing_Scoring_service_impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.champions_Call.model.Manging_Scoring.Football.FootballMatch;
import com.champions_Call.repository.Managing_Scoring.FootballRepository;
import com.champions_Call.service.managing_Scoring_services.FootballMatchService;

@Service 
public class FootballMatchServiceImpl implements FootballMatchService{

  @Autowired 
  private FootballRepository footballRepo;

  @Override
  public FootballMatch save(FootballMatch footballMatch) {
    return footballRepo.save(footballMatch);
  }

  @Override
  @Transactional // This ensures the database operations are rolled back together if a runtime exception occurs.
  public List<FootballMatch> saveAll(List<FootballMatch> footballMatch) {
    return footballRepo.saveAll(footballMatch);
  }

}
