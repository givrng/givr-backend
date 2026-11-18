package com.backend.givr.shared.dtos;

import com.backend.givr.shared.enums.DonationStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DonationUpdateDto(@NotBlank String donationId, @NotNull DonationStatus status) {
}
