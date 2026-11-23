package com.backend.givr.volunteer.dtos;

import com.backend.givr.shared.enums.IDType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDate;

public record VolunteerVerificationDto(@NotNull(message = "Id type is required") IDType idType, @NotBlank(message = "IdNumber is required") String idNumber, @NotBlank String firstname,
                                       String middleName, @NotBlank String lastname, LocalDate dateOfBirth, String individualLogo, @NotBlank @URL String callbackUrl) {
}
