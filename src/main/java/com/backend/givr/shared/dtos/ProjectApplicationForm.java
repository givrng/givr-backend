package com.backend.givr.shared.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProjectApplicationForm (@NotNull Long projectId, @NotBlank String reason, @NotBlank String availableDays){
}
