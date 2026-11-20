package com.champions_Call.service.impl.Managing_Scoring_service_impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.champions_Call.model.Manging_Scoring.Cricket.CricketMatch;
import com.champions_Call.repository.Managing_Scoring.CricketMatchRepository;
import com.champions_Call.service.managing_Scoring_services.CricketMatchService;

@Service 
public class CricketMatchServiceImpl implements CricketMatchService{

  @Autowired 
  private CricketMatchRepository cricketMatchRepo;

  @Override 
  public CricketMatch save(CricketMatch cricketMatch){
    return cricketMatchRepo.save(cricketMatch);
  }

  
  @Override
  @Transactional // This ensures the database operations are rolled back together if a runtime exception occurs.
  public List<CricketMatch> saveAll(List<CricketMatch> cricketMatch) {
    return cricketMatchRepo.saveAll(cricketMatch);
  }



}
