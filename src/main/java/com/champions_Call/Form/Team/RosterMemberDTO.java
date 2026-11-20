package com.champions_Call.Form.Team;

// Flat, safe object sent to the browser instead of the TeamRoster entity.
// It holds only plain values (no entity references), so it can never
// create a circular JSON loop.

public record RosterMemberDTO(
    Long id,             // roster row id
    String roleInTeam,   // CAPTAIN / VICE_CAPTAIN / PLAYER
    Long playerId,       // PlayerProfile id, used by openPlayerProfileModal()
    String fullName,     // display name
    String profileImage // avatar URL
){}