package com.champions_Call.service;

import com.champions_Call.model.User;

public interface UserServices {

  User saveUser(User user); 

  User getUserByEmail(String email);

  User getUserByPhoneNumber(String phoneNumber);

}
