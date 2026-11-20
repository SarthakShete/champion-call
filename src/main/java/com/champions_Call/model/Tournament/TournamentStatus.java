package com.champions_Call.model.Tournament;

public enum TournamentStatus{
    UNPUBLISHED, REGISTRATION_OPEN, ONGOING, COMPLETED,CANCELLED ;

    public static TournamentStatus fromString(String status) {
        if (status == null || status.isEmpty() || status.equals("ALL")) {
            return null;
        }   
        try {
            return TournamentStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            // Returns null if the string doesn't match any Enum value
            return null; 
        }
    }
    
}   


