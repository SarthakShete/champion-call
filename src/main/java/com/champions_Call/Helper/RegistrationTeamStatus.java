package com.champions_Call.Helper;

public enum RegistrationTeamStatus {
   
    PENDING_PAYMENT,   // Team submitted details, waiting for entry fee payment
    PAYMENT_SUBMITTED, // Transaction ID provided, waiting for verification
    CONFIRMED,         // Fee verified and registration accepted
    WAITLISTED,        // Tournament is full; team is in the queue if a spot opens up
    CHANGES_REQUESTED, // Organizer sent it back for minor corrections
    REJECTED,          // Organizer rejected the registration entirely
    CANCELLED,         // Cancelled by the team captain
    PENDING,
    APPROVED
    
}
