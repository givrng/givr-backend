package com.backend.givr.shared;

import com.backend.givr.organization.entity.Project;
import com.backend.givr.organization.entity.ProjectApplication;
import com.backend.givr.shared.enums.ApplicationStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.Date;
@NoArgsConstructor
@Getter
public class ProjectAppliedDto {
    private Long id;
    private String volunteer;
    private ApplicationStatus status;
    private String title;
    private ZonedDateTime appliedAt;

    public ProjectAppliedDto(ProjectApplication projectApplication){
        this.id = projectApplication.getId();
        this.status = projectApplication.getStatus();
        this.volunteer = projectApplication.getVolunteer().getVolunteerId();
        this.title = projectApplication.getProject().getTitle();
        this.appliedAt = projectApplication.getAppliedAt();
    }
}
