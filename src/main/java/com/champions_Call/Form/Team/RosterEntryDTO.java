package com.champions_Call.Form.Team;

import com.champions_Call.Form.Player.PlayerDetailDTO;

public record RosterEntryDTO(
  Long id, 
  String roleInTeam, 
  PlayerDetailDTO playerProfile) {
}
