package com.backend.givr.organization.dtos;

import com.backend.givr.shared.enums.ApplicationStatus;
import com.backend.givr.volunteer.dtos.VolunteerDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import java.time.ZonedDateTime;

@NoArgsConstructor
@Getter
@Setter
public class ProjectApplicationDto {
    private Long id;
    private VolunteerDto volunteer;
    private ProjectResponseDto project;
    private ApplicationStatus status;
    private ZonedDateTime appliedAt;
    private String applicationReason;
    private String availableDays;
}
