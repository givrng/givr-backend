package com.backend.givr.organization.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateOrganizationDto {
    @Email
    private String email;
    @NotBlank
    @Size(min = 6, message = "Password cannot be less than 6 characters")
    private String password;

    @NotBlank
    private String contactFirstname;

    private String contactMiddleName;
    @NotBlank
    private String contactLastname;

}
