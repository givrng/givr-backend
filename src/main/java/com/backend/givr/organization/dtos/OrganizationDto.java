package com.backend.givr.organization.dtos;

import com.backend.givr.shared.Location;
import com.backend.givr.shared.enums.VerificationStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OrganizationDto {
    private String id;

    private String email;

    @NotBlank
    private String phoneNumber;

    @NotBlank
    private String name;

    private int numOfActiveProjects;

    @NotBlank
    private List<String> category;

    @NotNull
    private LocationDto location;

    private Boolean profileCompleted;
    private String cacRegNumber;

    private String profileUrl;
    private VerificationStatus status;
    private String description;
    private String website;
    private String address;
}
