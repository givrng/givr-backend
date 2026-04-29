package com.backend.givr.shared.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ProjectApplicationForm (@NotNull Long projectId, @NotBlank String reason, @NotBlank String aboutMe, @NotNull Boolean isAvailable, List<String> mySkills, String additionalInfo){
}
