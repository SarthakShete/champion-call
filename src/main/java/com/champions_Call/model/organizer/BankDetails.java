package com.champions_Call.model.organizer;

import jakarta.persistence.Embeddable;

@Embeddable
public class BankDetails {

    private String accountHolderName;
    private String accountNumber;
    private String ifscCode;

    // Default constructor is required for JPA
    public BankDetails() {}

    // Getters and Setters
    public String getAccountHolderName() { return accountHolderName; }
    public void setAccountHolderName(String accountHolderName) { this.accountHolderName = accountHolderName; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public String getIfscCode() { return ifscCode; }
    public void setIfscCode(String ifscCode) { this.ifscCode = ifscCode; }
}