package com.champions_Call.service.impl.Managing_Scoring_service_impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.champions_Call.model.Manging_Scoring.Kabbadi.KabbadiMatch;
import com.champions_Call.repository.Managing_Scoring.KabbadiRepository;
import com.champions_Call.service.managing_Scoring_services.KabbadiMatchService;

import jakarta.transaction.Transactional;


@Service 
public class KabbadiMatchServiceImpl implements KabbadiMatchService{

  @Autowired 
  private KabbadiRepository kabbadiRepository;

  @Override
  public KabbadiMatch save(KabbadiMatch kabbadiMatch) {
      return kabbadiRepository.save(kabbadiMatch);
  }

  @Override
  @Transactional // This ensures the database operations are rolled back together if a runtime exception occurs.
  public List<KabbadiMatch> saveAll(List<KabbadiMatch> kabbadiMatch) {
     return kabbadiRepository.saveAll(kabbadiMatch);
  }

}
