package com.champions_Call.service.impl;

import com.champions_Call.service.ImageService;
import com.champions_Call.service.PlayerService;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import java.util.Optional;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.champions_Call.Helper.TournamentRole;
import com.champions_Call.model.User;
import com.champions_Call.model.player.PlayerProfile;
import com.champions_Call.repository.PlayerRepository;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import com.champions_Call.model.Team.TeamRoster;
import com.champions_Call.repository.TeamRosterRepository;

@Service
@Transactional
public class PlayerServiceImpl implements PlayerService{

  @Autowired
  private PlayerRepository playerRepo;

  @Autowired
    private ImageService imageService;

    @Autowired
    private Cloudinary cloudinary;

  @Autowired
  private TeamRosterRepository teamRosterRepo;

  @Override
  public Optional<PlayerProfile> getPlayerProfileByUser(User user){
    return playerRepo.findByUser(user);
  }

  @Override
  public boolean isUsernameExist(String username){
    return playerRepo.existsByUsername(username);
  }

  @Override
  public boolean isPhoneNumberExist(String phoneNumber){
    return playerRepo.existsByPhoneNumber(phoneNumber);
  }

  @Override
  public PlayerProfile savePlayerProfile(PlayerProfile player){
    return playerRepo.save(player);
  }

  @Override
  public void uploadProfileImage(Long profileId,MultipartFile file) throws IOException {

    PlayerProfile profile = playerRepo.findById(profileId)
            .orElseThrow(() -> new RuntimeException("Profile not found with ID: " + profileId));

    if(file.isEmpty()){
      throw new IllegalArgumentException("Cannot upload an empty file!");
    }

    String orginalFileName = file.getOriginalFilename();
    String uniqueFileName = UUID.randomUUID().toString()+"_"+orginalFileName;

    String fileUrl = imageService.uploadImage(file,uniqueFileName);
    profile.setProfileImage(fileUrl);
    profile.setCloudinaryImagePublicId(uniqueFileName);
  
    playerRepo.save(profile);

  }

  @Override
  public List<TeamRoster> getManagedTeamsForPlayer(PlayerProfile playerProfile){

    if(playerProfile==null || playerProfile.getId()==null){
      return List.of();
    }

    // Define leaderShip role...
    List<TournamentRole> leaderShipRoles = Arrays.asList(
      TournamentRole.CAPTAIN,
      TournamentRole.VICE_CAPTAIN
    );

    

    return teamRosterRepo.findByPlayerProfileAndRoleInTeamIn(playerProfile, leaderShipRoles);
  }

  public PlayerProfile getPlayerProfileById(Long id){
      return playerRepo.findById(id)
          .orElseThrow(() -> new RuntimeException("Player profile not found with ID: " + id));
  }
  

  @Override
  public void saveIdProof(User user, MultipartFile idDocument) {
      try {
          // 1. Get the player profile using findByUser
          PlayerProfile player = playerRepo.findByUser(user)
                  .orElseThrow(() -> new RuntimeException("Player profile not found"));

          // 2. Upload the file to Cloudinary
          Map uploadResult = cloudinary.uploader().upload(idDocument.getBytes(), ObjectUtils.emptyMap());
          String imageUrl = uploadResult.get("secure_url").toString();

          // 3. Update the player's ID proof field with the image URL
          player.setPlayerIdProof(imageUrl);

          // 4. Save the updated profile back to the database
          playerRepo.save(player);
      } catch (Exception e) {
          throw new RuntimeException("Failed to upload ID proof: " + e.getMessage());
      }
  }
}
