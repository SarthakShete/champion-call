package com.champions_Call.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

import com.champions_Call.model.player.Strength;

public interface StrengthRepository extends JpaRepository<Strength,Long>{
  List<Strength> findBySport(String sport);

}
