package com.backend.givr.organization.dtos;

import com.backend.givr.shared.Location;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateOrganizationDto {
    @Email
    private String email;
    @NotBlank
    @Size(min = 6, message = "Password cannot be less than 6 characters")
    private String password;

    @NotBlank
    private String contactFirstname;

    private String contactMiddleName;
    @NotBlank
    private String contactLastname;


    @Size(min = 11, max = 13, message = "Invalid phone number length")
    @NotBlank
    private String phoneNumber;

    @NotBlank
    private String organizationName;

    @NotBlank
    private String organizationType;

    @NotBlank
    private String cacRegNumber;

    @NotBlank
    private String driversLicenseNumber;

    @NotNull
    private Location location;

    private String description;
    private String website;
}
