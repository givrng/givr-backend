package com.backend.givr.shared.dtos;

import com.backend.givr.shared.enums.ParticipationStatus;
import com.backend.givr.volunteer.dtos.OrganizationResponseDTOv;
import com.backend.givr.volunteer.dtos.ProjectResponseDTOv;
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
    private ProjectResponseDTOv project;
    private OrganizationResponseDTOv organization;
    private VolunteerProfile volunteer;
    private Boolean reviewable;
    private String reason;
    private LocalDate endDate;
}
