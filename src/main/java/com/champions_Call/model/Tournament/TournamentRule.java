package com.champions_Call.model.Tournament;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="tournament_rules")
@Data
@AllArgsConstructor
public class TournamentRule {

    public TournamentRule() {
    }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int tournamentRuleId;

  @Column(nullable=false)
  private String ruleText;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name="tournament_id")
  private Tournament tournament;


  private String description;

    // This method is required by Thymeleaf
    public String getDescription() {
      System.out.println("Thymeleaf is calling getDescription!");
      return description;
    }

    public void setDescription(String description) {
      this.description = description;
    }

  // --- GETTERS ---
  public int getTournamentRuleId() { return tournamentRuleId; }
  public String getRuleText() { return ruleText; }
  public Tournament getTournament() { return tournament; }

  // --- SETTERS ---
  public void setTournamentRuleId(int tournamentListid) { this.tournamentRuleId = tournamentListid; }
  public void setRuleText(String ruleText) { this.ruleText = ruleText; }
  public void setTournament(Tournament tournament) { this.tournament = tournament; }




}
