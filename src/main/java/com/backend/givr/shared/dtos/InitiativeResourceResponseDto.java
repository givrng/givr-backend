package com.backend.givr.shared.dtos;

import java.util.List;

public record InitiativeResourceResponseDto(
        MediaDto media,
        List<InitiativeResourceDto> items,
        List<InitiativeResourceDto> services
) {
}
