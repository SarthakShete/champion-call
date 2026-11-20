package com.champions_Call.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankDetails {

    // SCREEN 3: Financial Settlement Details
    @Column(name = "account_holder_name")
    private String accountHolderName;

    @Column(name = "bank_account_number")
    private String bankAccountNumber;

    @Column(name = "ifsc_code")
    private String ifscCode;

    @Column(name = "bank_verified", nullable = false)
    @Builder.Default
    private boolean bankVerified = false; // Payout gateway validation flag
}