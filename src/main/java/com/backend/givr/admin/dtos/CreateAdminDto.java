package com.backend.givr.admin.dtos;

import com.backend.givr.admin.enums.AdminRole;
import jakarta.validation.constraints.Email;

public record CreateAdminDto(@Email String email, String name, AdminRole role) {
}
