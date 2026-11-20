package com.champions_Call.model.match;

import java.util.List;

public class ManualFixturesRequestDto {

    private List<ManualFixtureItemDto> fixtures;

    public List<ManualFixtureItemDto> getFixtures() {
        return fixtures;
    }

    public void setFixtures(List<ManualFixtureItemDto> fixtures) {
        this.fixtures = fixtures;
    }

    // Inner or separate DTO to catch the flat fields from JavaScript
    public static class ManualFixtureItemDto {
        private String roundNumber;
        private String matchDate;
        private String matchTime;
        private String venueLocation;
        private Long teamAId;
        private Long teamBId;
        private String status;

        // Getters and Setters
        public String getRoundNumber() { return roundNumber; }
        public void setRoundNumber(String roundNumber) { this.roundNumber = roundNumber; }

        public String getMatchDate() { return matchDate; }
        public void setMatchDate(String matchDate) { this.matchDate = matchDate; }

        public String getMatchTime() { return matchTime; }
        public void setMatchTime(String matchTime) { this.matchTime = matchTime; }

        public String getVenueLocation() { return venueLocation; }
        public void setVenueLocation(String venueLocation) { this.venueLocation = venueLocation; }

        public Long getTeamAId() { return teamAId; }
        public void setTeamAId(Long teamAId) { this.teamAId = teamAId; }

        public Long getTeamBId() { return teamBId; }
        public void setTeamBId(Long teamBId) { this.teamBId = teamBId; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}