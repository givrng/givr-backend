package com.backend.givr.shared.dtos;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

public record MinimalInitiativeDto(
        Long initiativeId,
        String title,
        ZonedDateTime createdAt
) {
}
