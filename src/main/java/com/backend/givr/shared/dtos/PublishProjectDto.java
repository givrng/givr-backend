package com.backend.givr.shared.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PublishProjectDto(
        @NotNull Long initiativeId,
        @NotBlank String callbackUrl
) {
}
