package com.champions_Call.repository.Managing_Scoring;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.champions_Call.model.Manging_Scoring.Kabbadi.KabbadiMatch;

@Repository 
public interface KabbadiRepository extends JpaRepository<KabbadiMatch,Long>{

}
