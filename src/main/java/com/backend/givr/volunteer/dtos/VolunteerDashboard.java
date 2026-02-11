package com.backend.givr.volunteer.dtos;

import com.backend.givr.organization.dtos.ProjectApplicationDto;
import java.util.List;

public record VolunteerDashboard(
        String firstname,
        Boolean profileCompleted,
        List<ProjectApplicationDto> projectApplications
) {

}
