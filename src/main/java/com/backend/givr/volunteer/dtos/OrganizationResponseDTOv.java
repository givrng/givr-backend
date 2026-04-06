package com.backend.givr.volunteer.dtos;

import com.backend.givr.organization.dtos.LocationDto;
import com.backend.givr.shared.enums.VerificationStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrganizationResponseDTOv {
    private String name;
    private LocationDto location;
    private VerificationStatus status;
    private String description;
    private String website;
    private String address;
}
