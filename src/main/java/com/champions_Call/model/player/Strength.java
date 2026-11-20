package com.champions_Call.model.player;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "strengths")
@Getter 
@Setter 
public class Strength {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name; // e.g., "Aggressive Batting"
    
    private String sport; // Optional: Link strength to a sport (CRICKET, FOOTBALL, etc.)

    public Strength() {
        // Required no-args constructor for JPA
    }

    public Strength(Long id, String name, String sport) {
        this.id = id;
        this.name = name;
        this.sport = sport;
    }

    // ===== Getters and Setters =====

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSport() {
        return sport;
    }

    public void setSport(String sport) {
        this.sport = sport;
    }

}
