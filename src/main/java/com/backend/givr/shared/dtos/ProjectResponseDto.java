package com.backend.givr.shared.dtos;

import com.backend.givr.organization.dtos.LocationDto;
import com.backend.givr.organization.dtos.OrganizationDto;
import com.backend.givr.organization.entity.AttendanceHours;
import com.backend.givr.shared.enums.ProjectStatus;
import com.backend.givr.shared.enums.ProjectType;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;

@Getter
@Setter
public class ProjectResponseDto {
    protected Long id;
    protected String title;

    protected OrganizationDto organization;
    protected String description;
    protected List<String> categories;
    protected Integer maxVolunteers;
    protected LocationDto location;
    protected String startDate;
    protected String endDate;
    protected String applicationDeadline;
    protected AttendanceHours attendanceHours;
    protected Set<String> requiredSkills;
    protected String specialRequirements;
    protected int totalApplicants;
    protected ProjectStatus status;
    protected ZonedDateTime createdAt;
    protected ZonedDateTime updatedAt;
    protected String address;
    protected Boolean broadcastEnabled;
    protected double rating;
    protected String projectFlierUrl;
    protected String projectCardUrl;
    protected ProjectType type;
}
