package com.champions_Call.Form.Managing_Score;

import java.util.List;

public record CricketMatchStateDTO(

        Long matchId,

        // Match information
        String matchStatus,
        String battingTeam,
        String bowlingTeam,

        // Innings score
        int totalRuns,
        int wickets,
        int overs,
        int balls,
        double runRate,
        int target,
        int runsRequired,

        // Current players
        Batter currentStriker,
        Batter currentNonStriker,
        Bowler currentBowler,

        // Recent deliveries
        List<String> recentBalls,

        // Extras
        int totalExtras,
        int wides,
        int noBalls,
        int byes,
        int legByes,

        // Current innings
        int inningsNumber

) {

    public record Batter(
            Long playerId,
            String playerName,
            int runs,
            int ballsFaced,
            int fours,
            int sixes,
            double strikeRate
    ) {}

    public record Bowler(
            Long playerId,
            String playerName,
            int overs,
            int balls,
            int runsConceded,
            int wickets,
            int maidens,
            double economy
    ) {}
}