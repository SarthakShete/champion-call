package com.champions_Call.service;

import com.champions_Call.Form.Managing_Score.CricketMatchStateDTO;
import com.champions_Call.Form.Managing_Score.CricketScoringRequest;

public interface CricketScoringService {

  // Processes the scoring event and updates the match score. We'll create this service separately.

    CricketMatchStateDTO processScoringEvent(CricketScoringRequest request);

    CricketMatchStateDTO getMatchState(Long matchId);
    
  
}
