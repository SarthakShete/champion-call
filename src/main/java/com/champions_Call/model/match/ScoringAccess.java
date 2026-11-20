package com.champions_Call.model.match;

import java.time.LocalDateTime;
import java.util.UUID;

import com.champions_Call.Helper.AccessDuration;
import com.champions_Call.Helper.AccessStatus;

import jakarta.persistence.*;


@Entity
@Table(name = "scoring_access")
public class ScoringAccess {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    // FIXED: was "Email" (capital E) — Java field names should start
    // lowercase; also this had no getter/setter at all before.
    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String scorerName;

    @Column(nullable = false)
    private String mobileNumber; // store full number; mask only at display/DTO layer

    // NEW: the credential used in the emailed link — /scorer/{accessToken}.
    // Whoever holds this token IS the authenticated scorer for this match,
    // for as long as status stays ACTIVE and accessDuration hasn't expired.
    @Column(nullable = false, unique = true, updatable = false)
    private String accessToken;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false,length = 30)
    private AccessDuration accessDuration;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false,length = 20)
    private AccessStatus status;

    @Column(nullable = false)
    private boolean mobileVerified = false;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime verifiedAt;

    private LocalDateTime revokedAt;

    // When the scorer's access ends (null = until match ends)
    private LocalDateTime expiresAt;

    // Wrong phone / wrong OTP counter. Locks the link after 5.
    @Column(name = "failed_attempts", nullable = false, columnDefinition = "int default 0")
    private int failedAttempts = 0;

    // OTP stored as a hash, never as plain text
    @Column(length = 64)
    private String otpHash;

    private LocalDateTime otpExpiresAt;

    // Given after OTP is correct. This is the token that allows scoring.
    @Column(unique = true, length = 64)
    private String sessionToken;


    // ---------- constructors ----------

    public ScoringAccess() {
        // required by JPA
    }

    // UPDATED: now takes email too, and generates the access token
    // automatically so callers never have to remember to set it.
    public ScoringAccess(Match match, String scorerName, String mobileNumber,
                          String email, AccessDuration accessDuration) {
        this.match = match;
        this.scorerName = scorerName;
        this.mobileNumber = mobileNumber;
        this.email = email;
        this.accessDuration = accessDuration;
        this.status = AccessStatus.PENDING; 
        this.mobileVerified = false;
        this.accessToken = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
    }

    // ---------- getters and setters ----------

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Match getMatch() {
        return match;
    }

    public void setMatch(Match match) {
        this.match = match;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getScorerName() {
        return scorerName;
    }

    public void setScorerName(String scorerName) {
        this.scorerName = scorerName;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
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

     public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public int getFailedAttempts() { return failedAttempts; }
    public void setFailedAttempts(int failedAttempts) { this.failedAttempts = failedAttempts; }

    public String getOtpHash() { return otpHash; }
    public void setOtpHash(String otpHash) { this.otpHash = otpHash; }

    public LocalDateTime getOtpExpiresAt() { return otpExpiresAt; }
    public void setOtpExpiresAt(LocalDateTime otpExpiresAt) { this.otpExpiresAt = otpExpiresAt; }

    public String getSessionToken() { return sessionToken; }
    public void setSessionToken(String sessionToken) { this.sessionToken = sessionToken; }
}