package com.champions_Call.service;

import com.champions_Call.model.*;
import com.champions_Call.model.organizer.OrganizerProfile;

import java.util.*;

public interface OrganizerService {

  OrganizerProfile saveOrganizer(OrganizerProfile organizerProfile);

  OrganizerProfile getUserbyId(int id);

  OrganizerProfile getByUser(User user);

  OrganizerProfile getByOrganizationName(String organizationName);

  OrganizerProfile getByPhoneNumber(String phoneNumber);

  boolean profileExists(User user);

  OrganizerProfile getCurrentProfile();

}
