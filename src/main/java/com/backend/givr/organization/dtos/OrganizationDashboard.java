package com.backend.givr.organization.dtos;

import java.util.List;

public record OrganizationDashboard(String name, List<ProjectDto> projects) {
}
