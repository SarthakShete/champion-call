package com.champions_Call.service.impl;

import java.util.Optional;

import com.champions_Call.model.*;
import com.champions_Call.model.organizer.OrganizerProfile;
import com.champions_Call.service.OrganizerService;
import com.champions_Call.repository.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Service
public class OrganizerServiceImpl implements OrganizerService{

    
    @Autowired
    private OrganizerProfileRepo organizerProfileRepo;

    @Autowired
    private UserRepo userRepo;

    @Override
    public OrganizerProfile saveOrganizer(OrganizerProfile organizerProfile){

      return organizerProfileRepo.save(organizerProfile);
    }

    @Override
    public OrganizerProfile getUserbyId(int id) {

      Optional<OrganizerProfile> organizerProfile = organizerProfileRepo.findByUserId(id);
        
      return organizerProfile.orElse(null);
    }

    @Override
    public OrganizerProfile getByUser(User user) {
        return organizerProfileRepo.findByUser(user)
                .orElse(null);
    }

    // ✅ 3. Get by Organization Name
    @Override
    public OrganizerProfile getByOrganizationName(String organizationName) {
        return organizerProfileRepo.findByOrganizationName(organizationName);
    }

    // ✅ 4. Get by Phone Number
    @Override
    public OrganizerProfile getByPhoneNumber(String phoneNumber) {
        return organizerProfileRepo.findByPhoneNumber(phoneNumber)
                .orElse(null);
    }

    // ✅ 5. Check if profile exists
    @Override
    public boolean profileExists(User user) {
      return organizerProfileRepo.existsByUser(user);
    }

    @Override
    public OrganizerProfile getCurrentProfile() {
        
      String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();

      User user = userRepo.findByEmail(currentEmail)
                        .orElseThrow(() -> new RuntimeException("User not found with email: " + currentEmail));

      return user.getOrganizerProfile();
    }
}
