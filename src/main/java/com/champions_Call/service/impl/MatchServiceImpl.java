package com.champions_Call.service.impl;

import com.champions_Call.model.Manging_Scoring.Cricket.CricketMatch;
import com.champions_Call.model.Manging_Scoring.Football.FootballMatch;
import com.champions_Call.model.Manging_Scoring.Kabbadi.KabbadiMatch;
import com.champions_Call.model.match.Match;
import com.champions_Call.repository.MatchRepository;
import com.champions_Call.service.MatchService;
import com.champions_Call.service.managing_Scoring_services.CricketMatchService;
import com.champions_Call.service.managing_Scoring_services.FootballMatchService;
import com.champions_Call.service.managing_Scoring_services.KabbadiMatchService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;


@Service
public class MatchServiceImpl implements MatchService{

  @Autowired
  private MatchRepository matchRepository;

  @Autowired 
  private CricketMatchService cricketMatchService;

  @Autowired 
  private FootballMatchService footballMatchService;

  @Autowired 
  private KabbadiMatchService kabbadiMatchService;


  public List<Match> saveAll(List<Match> matches){

    // Step 1: Save common Match records first
    List<Match> savedMatches =  matchRepository.saveAll(matches);

    // Step 2: Prepare sport-specific records
    List<CricketMatch> cricketMatches = new ArrayList<>();
    List<FootballMatch> footballMatches = new ArrayList<>();
    List<KabbadiMatch> kabaddiMatches = new ArrayList<>();

    for (Match match : savedMatches) {

        // BYE entries are not actual playable matches
        if (match.getTeamA() == null || match.getTeamB() == null) {
            continue;
        }

        String sport = match.getTournament().getSport();

        if (sport == null || sport.isBlank()) {
            throw new IllegalArgumentException(
                "Sport is not specified for tournament."
            );
        }

        // Cricket
        if ("cricket".equalsIgnoreCase(sport.trim())) {

            CricketMatch cricketMatch = new CricketMatch();
            cricketMatch.setMatch(match);

            cricketMatches.add(cricketMatch);
        }

        // Football
        else if ("football".equalsIgnoreCase(sport.trim())) {

            FootballMatch footballMatch = new FootballMatch();
            footballMatch.setMatch(match);

            footballMatches.add(footballMatch);
        }

        // Kabaddi
        else if ("kabaddi".equalsIgnoreCase(sport.trim())) {

            KabbadiMatch kabaddiMatch = new KabbadiMatch();
            kabaddiMatch.setMatch(match);

            kabaddiMatches.add(kabaddiMatch);
        }

        else {
            throw new IllegalArgumentException(
                "Unsupported sport: " + sport
            );
        }
    }

    // Step 3: Save sport-specific records
    cricketMatchService.saveAll(cricketMatches);
    footballMatchService.saveAll(footballMatches);
    kabbadiMatchService.saveAll(kabaddiMatches);

    return savedMatches;

  }

  @Override
  public Match save(Match match) {
      return matchRepository.save(match);
  }

  @Override
  public Match getById(Long tournamentId){
    return matchRepository.findById(tournamentId).orElse(null);
  }
  
  @Override
  public boolean existsByTournamentId(Long tournamentId) {
      return matchRepository.existsByTournamentId(tournamentId);
  }

  @Override
  public List<Match> getTeamsByTournamentId(Long tournamentId){
    return matchRepository.findByTournamentId(tournamentId);
  }

  @Override
  public Match ConflictMatch(String venueLocation, 
    LocalDate matchDate, 
    LocalTime matchTime, 
    Long id){

      return matchRepository.findFirstByVenueLocationAndMatchDateAndMatchTimeAndIdNot(venueLocation,matchDate,matchTime,id);
  }


  @Override
  public List<Match> getAllByTournamentOrderedByStatusPriority(Long tournamentId){
    
    return matchRepository.findAllByTournamentOrderedByStatusPriority(tournamentId);

  }
  
}
