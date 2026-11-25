package com.backend.givr.shared.dtos;

import com.backend.givr.organization.dtos.OrganizationDto;
import com.backend.givr.volunteer.dtos.VolunteerDto;

public record CreatorDto(
        OrganizationDto organization,
        VolunteerDto individual
) {
}
