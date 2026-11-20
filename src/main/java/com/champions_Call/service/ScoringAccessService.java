package com.champions_Call.service;

import java.util.List;

import com.champions_Call.Form.Match.ScoringAccessDTO;
import com.champions_Call.model.match.ScoringAccess;

public interface ScoringAccessService {


  ScoringAccess saveScorer(ScoringAccess scorer);

  ScoringAccess assignScorer(Long matchId,ScoringAccessDTO req);

  void resendAccess(Long matchId);

  List<ScoringAccess> getAllScorerMatchUsingTournamentID(Long tournamentId);

  ScoringAccess getCurrentScorer(Long matchId);

 

  void revokeAccess(Long matchId);
    
}
