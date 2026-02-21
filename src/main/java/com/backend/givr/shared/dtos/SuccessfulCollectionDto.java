package com.backend.givr.shared.dtos;

import com.backend.givr.shared.enums.MonnifyEventType;

public record SuccessfulCollectionDto(
        MonnifyEventType eventType
) {
}
