package com.champions_Call.model.organizer;

import com.champions_Call.model.Tournament.Tournament;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="tournament_prizes")
@Data
public class Prize{

  @Id
  @GeneratedValue(strategy=GenerationType.IDENTITY)
  private int prizeId;

  private String prizeName;
  private String prizeType;
  private String prizeDetails;

  @ManyToOne
  @JoinColumn(name="tournament_id")
  private Tournament tournament;


  // Getters
  public int getPrizeId() { return prizeId; }
  public String getPrizeName() { return prizeName; }
  public String getPrizeType() { return prizeType; }
  public String getPrizeDetails() { return prizeDetails; }
  public Tournament getTournament() { return tournament; }

  // Setters
  public void setPrizeId(int prizeId) { this.prizeId = prizeId; }
  public void setPrizeName(String prizeName) { this.prizeName = prizeName; }
  public void setPrizeType(String prizeType) { this.prizeType = prizeType; }
  public void setPrizeDetails(String prizeDetails) { this.prizeDetails = prizeDetails; }
  public void setTournament(Tournament tournament) { this.tournament = tournament; }


}
