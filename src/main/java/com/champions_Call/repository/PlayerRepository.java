package com.champions_Call.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.champions_Call.model.User;
import com.champions_Call.model.player.PlayerProfile;
import java.util.Optional;

public interface PlayerRepository extends JpaRepository<PlayerProfile,Long>{

  Optional<PlayerProfile> findByUser(User user);

  boolean existsByUsername(String username);
  
  boolean existsByPhoneNumber(String phoneNumber);

}
