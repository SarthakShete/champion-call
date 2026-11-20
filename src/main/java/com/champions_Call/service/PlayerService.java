package com.champions_Call.service;

import java.util.Optional;

import org.springframework.web.multipart.MultipartFile;

import com.champions_Call.model.User;
import com.champions_Call.model.Team.TeamRoster;
import com.champions_Call.model.player.PlayerProfile;

import java.io.IOException;
import java.util.List;

public interface PlayerService {

  Optional<PlayerProfile> getPlayerProfileByUser(User user);

  boolean isUsernameExist(String username);

  boolean isPhoneNumberExist(String phoneNumber);

  PlayerProfile savePlayerProfile(PlayerProfile playerProfile);

  void uploadProfileImage(Long profileId,MultipartFile file) throws IOException;

  List<TeamRoster> getManagedTeamsForPlayer(PlayerProfile playerProfile);

  PlayerProfile getPlayerProfileById(Long id);

  void saveIdProof(User user,MultipartFile idDocument);

}
