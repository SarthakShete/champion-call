package com.champions_Call.service.impl;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.champions_Call.model.User;
import com.champions_Call.repository.UserRepo;
import com.champions_Call.service.UserServices;

@Service
public class UserServicesImpl implements UserServices{

  @Autowired
  private UserRepo userRepo;

  @Override
  public User saveUser(User user){
    return userRepo.save(user);
  }

  @Override
  public User getUserByEmail(String email){

    Optional<User> user = userRepo.findByEmail(email);

    return user.orElse(null);
  
  }

  @Override
  public User getUserByPhoneNumber(String phone){

    Optional<User> user = userRepo.findByPhoneNumber(phone);

    return user.orElse(null);
  }
}
