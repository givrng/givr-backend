package com.backend.givr.shared.dtos;

import com.backend.givr.shared.enums.AccountType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordResetDto(@Email String email, AccountType role, @Size(min = 6, message = "Password must be at least 6 characters long") String newPassword, @NotBlank String otp) {
}
