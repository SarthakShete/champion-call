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
public class IdentityVerification {

    

  // SCREEN 2: Identity Verification Details
    @Column(name = "id_type")
    private String idType; // e.g., "PAN", "AADHAR"

    @Column(name = "id_number")
    private String idNumber;

    @Column(name = "document_url")
    private String documentUrl; // Stores the file path or S3 link of the uploaded ID image

    @Column(name = "identity_verified", nullable = false)
    @Builder.Default
    private boolean identityVerified = false; // Admin approval flag

   

}
