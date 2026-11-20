package com.champions_Call.Form.Team;

public class JoinTeamRequestDto {
    private Long teamRegistrationId;
    private String inviteCode;

    // Getters and Setters
    public Long getTeamRegistrationId() {
        return teamRegistrationId;
    }

    public void setTeamRegistrationId(Long teamRegistrationId) {
        this.teamRegistrationId = teamRegistrationId;
    }

    public String getInviteCode() {
        return inviteCode;
    }

    public void setInviteCode(String inviteCode) {
        this.inviteCode = inviteCode;
    }
}