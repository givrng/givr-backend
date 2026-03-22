package com.backend.givr.organization.dtos;

import com.backend.givr.shared.enums.IDType;
import com.backend.givr.shared.enums.VerificationStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class OrganizationUpdateDto {
    // Organization-Specific information
    // name -> organizationName;
    // category -> organizationType;

    public static record ContactVerification (IDType idType, String idNumber, String usrImgUrl) {

    }

    private String name;
    private List<String> category;
    private LocationDto location;
    private String profileUrl;
    private VerificationStatus status;
    private String cacRegNumber;
    private String description;
    private String website;
    private String address;
    private String cacDocUrl;
    // Organization Contact specific-information
    @Email
    private String email;
    private String contactFirstname;
    private String contactLastname;
    private String contactMiddleName;
    private String dateOfBirth;
    @Size(min = 11, max = 13, message = "Invalid phone number")
    private String phoneNumber;

    private ContactVerification contactVerification;
}
