package com.backend.givr.admin.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReviewDto(
        @NotBlank String reason,
        @NotNull Boolean isApproved,
        @NotNull Long sessionId
) {
}
