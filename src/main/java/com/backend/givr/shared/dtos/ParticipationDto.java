package com.backend.givr.shared.dtos;

import com.backend.givr.organization.dtos.OrganizationDto;
import com.backend.givr.organization.dtos.ProjectResponseDto;
import com.backend.givr.shared.enums.ParticipationStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ParticipationDto {
    private long id;
    private ParticipationStatus status;
    private ProjectResponseDto project;
    private OrganizationDto organization;
}
