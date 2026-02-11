package com.backend.givr.volunteer.dtos;

import com.backend.givr.shared.Location;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;
import java.util.Objects;

@Data
public class CreateVolunteerRequestDto {
    @NotBlank
    private String firstname;
    private String middleName;
    @NotBlank
    private String lastname;
    @Email
    private String email;

    @NotNull
    @Size(min = 11, max = 13, message = "Invalid number, no less than 10 and no more than 13")
    private String phone;

    @NotNull
    @Size(min = 6, message = "Password is too short")
    private String password;
    @Size(min = 6, message = "Password is too short")
    private String confirmPassword;

    @NotNull
    private Location location;
    private String profileUrl;
    @NotNull
    private List<String> interests;
    public boolean validatePassword(){
        return Objects.equals(this.password, this.confirmPassword);
    }
}
