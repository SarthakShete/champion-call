package com.champions_Call.Form.Match;

import com.champions_Call.model.match.Match;
import java.time.LocalDate;
import java.util.List;

public class ScheduleDayDTO {
    private LocalDate date;
    private List<Match> matches;

    public ScheduleDayDTO(LocalDate date, List<Match> matches) {
        this.date = date;
        this.matches = matches;
    }

    // Getters and Setters
    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public List<Match> getMatches() {
        return matches;
    }

    public void setMatches(List<Match> matches) {
        this.matches = matches;
    }
}
