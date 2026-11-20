package com.champions_Call.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import com.champions_Call.model.User;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepo extends JpaRepository<User, Integer>{

  Optional<User> findByEmail(String email);

  Optional<User> findByPhoneNumber(String PhoneNumber);
}
