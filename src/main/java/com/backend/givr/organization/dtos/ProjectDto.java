package com.backend.givr.organization.dtos;

import com.backend.givr.shared.Location;
import com.backend.givr.shared.enums.ProjectStatus;
import com.fasterxml.jackson.annotation.JsonSetter;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ProjectDto {
    Long id;
    
    @NotBlank
    private String title;

    @NotBlank
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

    @NotBlank
    private String attendanceHours;

    @NotNull
    private List<String> requiredSkills;

    private LocationDto locationDto;

    private String specialRequirements;

    private ProjectStatus status;

    @JsonSetter(value = "category")
    private void setCat(String category){
        this.categories = List.of(category.trim());
    }
}
