package com.backend.givr.shared.dtos;

import com.backend.givr.organization.dtos.OrganizationDto;
import com.backend.givr.volunteer.dtos.PublicVolunteerDto;

public record CreatorDto(
        OrganizationDto organization,
        PublicVolunteerDto individual
) {
}
