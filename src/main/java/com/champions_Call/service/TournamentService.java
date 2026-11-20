package com.champions_Call.service;


import java.util.List;

import com.champions_Call.model.match.ManualFixturesRequestDto;
import com.champions_Call.model.match.Match;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import com.champions_Call.model.player.PlayerProfile;

import com.champions_Call.model.Tournament.Tournament;

public interface TournamentService {
    
  Tournament saveTournament(Tournament tournament);
  
  Tournament getTournamentId(Long id);

  boolean isTournamentNameExist(String TournamentName);

  boolean isTournamentNameExistAndIdNot(String tournamentName,Long id);

  List<Tournament> getAllTournaments();

  List<Tournament> getTournamentsByOrganizer(Long id);

  List<Tournament> searchByFilters(Long organizerId,String keyword,String sport,String status);

  public boolean canPublish(int OrganizerId);

  Page<Tournament> findAll(Specification<Tournament> spec,Pageable pageable);

  List<Tournament> getTournamentsForPlayer(PlayerProfile player);

  void generateFixturesForTournament(Long tournamentId);

  void saveManualFixturesBatch(Long tournamentId,ManualFixturesRequestDto rawMatches);
}
