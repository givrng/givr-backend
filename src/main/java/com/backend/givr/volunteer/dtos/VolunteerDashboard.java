package com.backend.givr.volunteer.dtos;

import com.backend.givr.organization.entity.ProjectApplication;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class VolunteerDashboard {
    @JsonManagedReference
    private final String firstname;

    @JsonManagedReference
    private final List<ProjectApplication> projectApplications;
}
