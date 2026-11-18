package com.backend.givr.shared.dtos;

public record InitiativeResourceDto(
        String resource,
        String resourceId,
        int quantity
) {
}
