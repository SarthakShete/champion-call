package com.champions_Call.Configuration;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.champions_Call.model.player.Strength;
import com.champions_Call.repository.StrengthRepository;

@Component
public class StrengthSeeder implements CommandLineRunner {

    @Autowired
    private StrengthRepository strengthRepo;

    @Override
    public void run(String... args) {
        if (strengthRepo.count() > 0) {
            return; // already seeded, don't duplicate on every restart
        }

        List<Strength> strengths = List.of(
            // CRICKET
            new Strength(null, "Aggressive Batting", "CRICKET"),
            new Strength(null, "Expert at Chasing", "CRICKET"),
            new Strength(null, "Swing Bowling", "CRICKET"),
            new Strength(null, "Spin Bowling", "CRICKET"),
            new Strength(null, "Wicketkeeping", "CRICKET"),

            // FOOTBALL
            new Strength(null, "Speed", "FOOTBALL"),
            new Strength(null, "Dribbling", "FOOTBALL"),
            new Strength(null, "Defensive Wall", "FOOTBALL"),
            new Strength(null, "Playmaking", "FOOTBALL"),
            new Strength(null, "Goal Scoring", "FOOTBALL"),

            // KABADDI
            new Strength(null, "Raiding", "KABADDI"),
            new Strength(null, "Ankle Hold", "KABADDI"),
            new Strength(null, "Thigh Hold", "KABADDI"),
            new Strength(null, "Chain Tackle", "KABADDI"),
            new Strength(null, "Fast Reflexes", "KABADDI")
        );

        strengthRepo.saveAll(strengths);
    }
}