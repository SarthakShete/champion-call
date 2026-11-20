package com.champions_Call.model.organizer;

import jakarta.persistence.Embeddable;

@Embeddable
public class IdentityVerification {

    private String idType;
    private String idNumber;
    private String documentUrl;
    private boolean verified = false;

    

    // Default constructor is required for JPA
    public IdentityVerification() {}

    // Getters and Setters
    public String getIdType() { return idType; }
    public void setIdType(String idType) { this.idType = idType; }

    public String getIdNumber() { return idNumber; }
    public void setIdNumber(String idNumber) { this.idNumber = idNumber; }

    public String getDocumentUrl() { return documentUrl; }
    public void setDocumentUrl(String documentUrl) { this.documentUrl = documentUrl; }

    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }
}