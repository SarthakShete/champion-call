package com.champions_Call.Form.organizer;

public class NotificationDTO {

    private String title;
    private String message;
    private String matchUrl;

    // Constructors, Getters, and Setters
    public NotificationDTO(String title, String message, String matchUrl) {
        this.title = title;
        this.message = message;
        this.matchUrl = matchUrl;
    }

    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public String getMatchUrl() { return matchUrl; }
} 
