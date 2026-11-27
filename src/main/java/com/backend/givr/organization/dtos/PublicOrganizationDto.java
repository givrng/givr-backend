package com.backend.givr.organization.dtos;

import com.backend.givr.shared.enums.VerificationStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PublicOrganizationDto {
    private String organizationId;

    @NotBlank
    private String name;
    private VerificationStatus status;
    private String description;
    private String website;

}
