package com.backend.givr.organization.dtos;

import java.util.List;
import java.util.Map;

public record OrganizationDashboard(String name, Map<String, List<ProjectResponseDto>> projects, double rating, ApplicationStats applicationStats, boolean isRestricted) {
}
