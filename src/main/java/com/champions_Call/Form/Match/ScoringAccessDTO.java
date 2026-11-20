package com.champions_Call.Form.Match;

import java.time.LocalDateTime;

import com.champions_Call.Helper.AccessDuration;
import com.champions_Call.Helper.AccessStatus;
import com.champions_Call.model.match.ScoringAccess;

public class ScoringAccessDTO {

    private Long id;
    private Long matchId;
    private String scorerName;
    private String email;
    private String mobileNumber; // real number — used when the organizer submits the Assign Scorer form
    private String maskedMobile; // e.g. "******3210" — set when returning data for display (View Access)
    private AccessDuration accessDuration;
    private AccessStatus status;
    private boolean mobileVerified;

    //It stores the date and time when the organizer assigned the scorer.
    private LocalDateTime createdAt;
    private LocalDateTime verifiedAt;

    private LocalDateTime revokedAt;


    // Suppose the organizer assigned Rahul as scorer:

    // 8:00 PM → Access created
    // 9:30 PM → Organizer cancels Rahul's access

    // Then:

    // createdAt = 8:00 PM
    // revokedAt = 9:30 PM
    
   // So:  revokedAt = "When did the organizer cancel this scorer's access?"

    // NOTE: accessToken is intentionally NOT included here. That token is
    // the scorer's credential — it only ever goes into the emailed link,
    // never into a JSON response the organizer's browser can read.

    //Rem : 
    // The accessToken is basically a secret unique key that proves the person has permission to access the scoring system.

    public ScoringAccessDTO() {
    }

    // ---------- mapper ----------
    // Used when RETURNING data (e.g. View Access). Fills maskedMobile,
    // leaves mobileNumber null so the full number doesn't round-trip
    // back to the browser unnecessarily.

    public static ScoringAccessDTO fromEntity(ScoringAccess entity) {
        ScoringAccessDTO dto = new ScoringAccessDTO();
        dto.setId(entity.getId());
        dto.setMatchId(entity.getMatch() != null ? entity.getMatch().getId() : null);
        dto.setScorerName(entity.getScorerName());
        dto.setEmail(entity.getEmail());
        dto.setMaskedMobile(maskMobile(entity.getMobileNumber()));
        dto.setAccessDuration(entity.getAccessDuration());
        dto.setStatus(entity.getStatus());
        dto.setMobileVerified(entity.isMobileVerified());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setVerifiedAt(entity.getVerifiedAt());
        dto.setRevokedAt(entity.getRevokedAt());
        return dto;
    }

    private static String maskMobile(String mobile) {
        if (mobile == null || mobile.length() < 4) return "******";
        return "******" + mobile.substring(mobile.length() - 4);
    }

    // ---------- getters and setters ----------

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMatchId() {
        return matchId;
    }

    public void setMatchId(Long matchId) {
        this.matchId = matchId;
    }

    public String getScorerName() {
        return scorerName;
    }

    public void setScorerName(String scorerName) {
        this.scorerName = scorerName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getMaskedMobile() {
        return maskedMobile;
    }

    public void setMaskedMobile(String maskedMobile) {
        this.maskedMobile = maskedMobile;
    }

    public AccessDuration getAccessDuration() {
        return accessDuration;
    }

    public void setAccessDuration(AccessDuration accessDuration) {
        this.accessDuration = accessDuration;
    }

    public AccessStatus getStatus() {
        return status;
    }

    public void setStatus(AccessStatus status) {
        this.status = status;
    }

    public boolean isMobileVerified() {
        return mobileVerified;
    }

    public void setMobileVerified(boolean mobileVerified) {
        this.mobileVerified = mobileVerified;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(LocalDateTime verifiedAt) {
        this.verifiedAt = verifiedAt;
    }

    public LocalDateTime getRevokedAt() {
        return revokedAt;
    }

    public void setRevokedAt(LocalDateTime revokedAt) {
        this.revokedAt = revokedAt;
    }
}