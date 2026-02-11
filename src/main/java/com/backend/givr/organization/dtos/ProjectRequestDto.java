package com.backend.givr.organization.dtos;

import com.backend.givr.organization.entity.AttendanceHours;
import com.fasterxml.jackson.annotation.JsonSetter;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ProjectRequestDto {
    @NotBlank
    private String title;

    private String description;

    @NotNull
    private List<String> categories;

    @Min(value = 1, message = "Cannot create a project for no volunteer")
    private Integer maxVolunteers;

    @NotNull
    private LocationDto location;

    @NotNull
    private String startDate;
    @NotNull
    private String endDate;
    @NotNull
    private String applicationDeadline;

    @NotNull
    private AttendanceHours attendanceHours;

    @NotNull
    private List<String> requiredSkills;

//    private LocationDto locationDto;
//
//    private String specialRequirements;
//
//    private int totalApplicants;
//
//    private ProjectStatus status;
//
//    private String organizationId;
//    private ZonedDateTime createdAt;
//    private ZonedDateTime updatedAt;

    @JsonSetter(value = "category")
    private void setCat(String category){
        this.categories = List.of(category.trim());
    }
}
