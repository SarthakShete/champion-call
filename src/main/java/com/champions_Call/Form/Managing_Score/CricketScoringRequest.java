package com.champions_Call.Form.Managing_Score;

public record CricketScoringRequest(

        // Match and event identification
        Long matchId,
        String eventId,
        ActionType actionType,

        // Runs scored
        int runsOffBat,
        int completedRuns,

        // Extras
        ExtraType extraType,
        int extraRuns,

        // Wicket information
        boolean wicket,
        WicketType wicketType,
        Long dismissedBatterId,
        Long newBatterId,
        Long fielderId,

        // Undo information
        String targetEventId
) {

    public enum ActionType {
        BALL,
        SWAP_STRIKE,
        UNDO
    }

    public enum ExtraType {
        NONE,
        WIDE,
        NO_BALL,
        BYE,
        LEG_BYE
    }

    public enum WicketType {
        BOWLED,
        CAUGHT,
        LBW,
        RUN_OUT,
        STUMPED,
        HIT_WICKET,
        RETIRED_OUT,
        OTHER
    }
}
