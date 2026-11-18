package com.backend.givr.shared.dtos;

import java.time.Duration;

public record ServiceDto(
        String serviceName,
        int quantity
) {
}
