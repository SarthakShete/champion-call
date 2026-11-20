package com.champions_Call.Form.organizer;

import org.springframework.web.multipart.MultipartFile;

import com.champions_Call.Validators.ValidFile;
import com.champions_Call.model.BankDetails;
import com.champions_Call.model.IdentityVerification;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class OrganizerBasicInfoForm {

    @NotBlank(message = "Organization name is Required")
    private String organizationName;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be exactly 10 digits")
    private String phoneNumber;

    @ValidFile
    private MultipartFile profileImage;

    // --- Step 2: Identity ---
    private IdentityVerification identity = new IdentityVerification();

    // --- Step 3: Bank Details ---
    private BankDetails bankDetails = new BankDetails();


    // ---------------- GETTERS & SETTERS ----------------

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

    public MultipartFile getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(MultipartFile profileImage) {
        this.profileImage = profileImage;
    }

    public IdentityVerification getIdentity() { return identity; }
    public void setIdentity(IdentityVerification identity) { this.identity = identity; }

    // Bank Details Getters/Setters
    public BankDetails getBankDetails() { return bankDetails; }
    public void setBankDetails(BankDetails bankDetails) { this.bankDetails = bankDetails; }


    @Override
    public String toString() {
        return "OrganizerBasicInfoForm{" +
                "organizationName='" + organizationName + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", profileImage=" + (profileImage != null ? profileImage.getOriginalFilename() : "null") +
                '}';
    }
}