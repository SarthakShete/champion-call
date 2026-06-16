package com.champions_Call.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.champions_Call.repository.UserRepo;

@Service
public class SecurityCustomUserDetailsService implements UserDetailsService{

  @Autowired
  private UserRepo userRepo;

  


  // Remember -> loadUserByUsername fetches the full UserDetails based on the username, not just the username itself.

  
  @Override
  public UserDetails loadUserByUsername(String username)
          throws UsernameNotFoundException {

      // return  UserDetails...
      //  user class should implemented the UserDetails
      // Because : 
      //         1) loadUserByUsername() return type is UserDetails
      //         2) You are returning user
      //         3) So user must be assignable to UserDetails
      
      var user = userRepo.findByEmail(username)
          .orElseThrow(() ->
              new UsernameNotFoundException("User not found"));


      return user;
  }

}
