package com.champions_Call.model.organizer;

import com.champions_Call.model.BankDetails;
import com.champions_Call.model.organizer.IdentityVerification;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.champions_Call.model.User;
import org.springframework.util.StringUtils;    

import jakarta.persistence.*;

@Entity
@Table(name = "organizer_profiles")
public class OrganizerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", unique = true, nullable = false)
    @JsonIgnore
    private User user;

    // Screen 1 : Basic Info
    private String profilePicture;
    private String organizationName;
    private String phoneNumber;

    @Column(name = "complete_Profile", nullable = false)
    private boolean completeProfile = false;

    @Column(name = "cloudinary_image_public_id")
    private String cloudinaryImagePublicId;

    // Screen 2 : Identity
    @Embedded
    private IdentityVerification identity = new IdentityVerification();

    public OrganizerProfile() {
    // Leave this empty!
    }

    // 2. Use this for your own code when you need a "fresh" profile
    public void initializeDefaults() {
        if (this.identity == null) {
            this.identity = new IdentityVerification();
        }
        if (this.bankDetails == null) {
            this.bankDetails = new BankDetails();
        }
    }

    @Embedded
    private BankDetails bankDetails = new BankDetails();

    public boolean isIdentityVerified() {
        // 1. Identity must not be null (
        // 2. We check that both idType and idNumber contain actual text
        return this.identity != null 
            && StringUtils.hasText(this.identity.getIdType())
            && StringUtils.hasText(this.identity.getIdNumber());
    }
    // ---------------- GETTERS & SETTERS ----------------

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public boolean isCompleteProfile() {
        return completeProfile;
    }

    public void setCompleteProfile(boolean completeProfile) {
        this.completeProfile = completeProfile;
    }

    public String getCloudinaryImagePublicId() {
        return cloudinaryImagePublicId;
    }

    public void setCloudinaryImagePublicId(String cloudinaryImagePublicId) {
        this.cloudinaryImagePublicId = cloudinaryImagePublicId;
    }

    public IdentityVerification getIdentity() {
        return identity;
    }

    public void setIdentity(IdentityVerification identity) {
        this.identity = identity;
    }

    public BankDetails getBankDetails() {
        return bankDetails;
    }

    public void setBankDetails(BankDetails bankDetails) {
        this.bankDetails = bankDetails;
    }

    // Add this method inside your OrganizerProfile class
    public String getDisplayProfilePicture() {
    // 1. Check if it's not null, not empty, AND not the placeholder URL
    boolean hasValidPicture = this.profilePicture != null 
                              && !this.profilePicture.trim().isEmpty() 
                              && !this.profilePicture.contains("ui-avatars.com");

    if (hasValidPicture) {
        return this.profilePicture;
    }
    
    // 2. Fallback to generating a new avatar
    String name = (this.organizationName != null && !this.organizationName.trim().isEmpty()) 
                  ? this.organizationName.trim() 
                  : "Organizer";
    
    return "https://ui-avatars.com/api/?name=" + name.replace(" ", "+") + 
           "&background=151518&color=ff914d";
}
}