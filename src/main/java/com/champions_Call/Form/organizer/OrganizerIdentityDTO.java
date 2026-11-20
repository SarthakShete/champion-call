package com.champions_Call.Form.organizer;

import org.springframework.web.multipart.MultipartFile;

import com.champions_Call.Validators.ValidFile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class OrganizerIdentityDTO {

    @NotBlank(message="Id Type cannot be Empty")
    private String idType;

    @NotBlank(message="Id Number cannot be Empty")
    @Pattern(regexp = "^([0-9]{12}|[A-Z]{5}[0-9]{4}[A-Z]{1})$", 
             message = "Invalid format: Aadhar must be 12 digits, PAN must be 10 alphanumeric chars")
    private String idNumber;

    @ValidFile
    private String documentUrl;

    public String getIdType() {
        return idType;
    }

    public void setIdType(String idType) {
        this.idType = idType;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }

    public String getDocumentUrl() { return documentUrl; }
    public void setDocumentUrl(String documentUrl) { this.documentUrl = documentUrl; }
}
