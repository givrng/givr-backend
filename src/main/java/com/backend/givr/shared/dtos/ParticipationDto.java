package com.backend.givr.shared.dtos;

import com.backend.givr.organization.dtos.OrganizationDto;
import com.backend.givr.organization.dtos.ProjectResponseDto;
import com.backend.givr.shared.enums.ParticipationStatus;
import com.backend.givr.volunteer.dtos.VolunteerProfile;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@NoArgsConstructor
@Getter
@Setter
public class ParticipationDto {
    private long id;
    private ParticipationStatus status;
    private ProjectResponseDto project;
    private OrganizationDto organization;
    private VolunteerProfile volunteer;
    private Boolean reviewable;
    private LocalDate endDate;
}
