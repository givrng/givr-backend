package com.backend.givr.volunteer.dtos;

import com.backend.givr.organization.dtos.LocationDto;
import com.backend.givr.organization.entity.AttendanceHours;
import com.backend.givr.shared.enums.ProjectStatus;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;

/**
 * This Dto exists to prevent the cyclic error that would occur
 * when using ProjectResponseDTOv and OrganizationResponseDTOv*/
@Getter
@Setter
public class ProjectViewResponse {
    Long id;
    private String title;
    private String description;
    private List<String> categories;
    private Integer maxVolunteers;
    private LocationDto location;
    private String startDate;
    private String endDate;
    private String applicationDeadline;
    private AttendanceHours attendanceHours;
    private Set<String> requiredSkills;
    private String specialRequirements;
    private int totalApplicants;
    private ProjectStatus status;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    private String address;
    private double rating;

}
