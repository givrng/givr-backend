package com.backend.givr.organization.dtos;

import com.backend.givr.shared.enums.ApplicationStatus;
import com.backend.givr.volunteer.dtos.VolunteerDto;
import com.backend.givr.volunteer.dtos.VolunteerProfile;


import java.time.ZonedDateTime;

public class ProjectApplicationDto {
    private Long id;

    private VolunteerDto volunteer;

    private ProjectDto project;


    private ApplicationStatus status;

    private ZonedDateTime appliedAt;

    private String applicationReason;
    private String availableDays;
}
