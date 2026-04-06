package com.backend.givr.volunteer.dtos;

import com.backend.givr.organization.dtos.LocationDto;
import com.backend.givr.organization.dtos.OrganizationDto;
import com.backend.givr.organization.entity.AttendanceHours;
import com.backend.givr.shared.enums.ProjectStatus;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;

@Getter
@Setter
public class ProjectResponseDTOv {
    Long id;
    private String title;

    private OrganizationResponseDTOv organization;
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
    private Boolean broadcastEnabled;
    private double rating;

    @JsonSetter(value = "category")
    private void setCat(String category){
        this.categories = List.of(category.trim());
    }
}
