package com.backend.givr.shared.dtos;

import com.backend.givr.shared.enums.AccountType;
import jakarta.validation.constraints.Email;

public record OtpRequestDto(@Email String email, AccountType role) {
}
