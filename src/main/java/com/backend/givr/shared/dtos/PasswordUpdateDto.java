package com.backend.givr.shared.dtos;

import jakarta.validation.constraints.Size;

public record PasswordUpdateDto(String otp, @Size(min = 6) String password, @Size(min = 6) String rePassword) {
}
