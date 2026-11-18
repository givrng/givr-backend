package com.backend.givr.shared.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ProjectApplicationForm (@NotNull Long projectId, @NotBlank String reason, @NotBlank String aboutMe, @NotNull Boolean isAvailable, List<String> mySkills, String additionalInfo, @Size(min = 11, max = 16, message = "Phone number must be between 11 to 16 digits long") String phoneNumber){
}
