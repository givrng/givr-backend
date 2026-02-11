package com.backend.givr.organization.dtos;

import com.backend.givr.organization.entity.AttendanceHours;
import com.backend.givr.shared.enums.ProjectStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;

@Getter
@Setter
public class ProjectResponseDto {
    Long id;
    private String title;

    private OrganizationDto organization;
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

    @JsonSetter(value = "category")
    private void setCat(String category){
        this.categories = List.of(category.trim());
    }
}
