package com.backend.givr.organization.dtos;

import com.backend.givr.shared.enums.ParticipationStatus;

public record UpdateParticipantDto(Long id, ParticipationStatus status) {
}
