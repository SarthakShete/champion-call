package com.champions_Call.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.champions_Call.model.*;
import com.champions_Call.model.organizer.OrganizerProfile;

import org.springframework.stereotype.Repository;

@Repository
public interface OrganizerProfileRepo extends JpaRepository<OrganizerProfile,Integer>{

  Optional<OrganizerProfile> findByUserId(int userId);  

  OrganizerProfile findByOrganizationName(String organizationName);

  Optional<OrganizerProfile> findByPhoneNumber(String phoneNumber);
  
  // This will generate: SELECT * FROM organizer_profile WHERE user_id = ?
  Optional<OrganizerProfile> findByUser(User user);

  boolean existsByUser(User user);
}
