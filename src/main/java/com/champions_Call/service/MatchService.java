package com.champions_Call.service;

import org.springframework.beans.factory.annotation.Autowired;

import com.champions_Call.model.match.Match;
import com.champions_Call.repository.MatchRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface MatchService {

 
  List<Match> saveAll(List<Match> matches);

  Match save(Match match);

  Match getById(Long id);

  // List<Match> getMatchesByTournamentId(Long tournamentId);


  boolean existsByTournamentId(Long tournamentId);

  List<Match> getTeamsByTournamentId(Long tournamentId);

  Match ConflictMatch(String venueLocation, 
    LocalDate matchDate, 
    LocalTime matchTime, 
    Long id);

  List<Match> getAllByTournamentOrderedByStatusPriority(Long tournamentId);
  
}
