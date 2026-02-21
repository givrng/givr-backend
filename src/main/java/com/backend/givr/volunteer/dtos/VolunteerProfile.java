package com.backend.givr.volunteer.dtos;

import com.backend.givr.organization.dtos.LocationDto;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class VolunteerProfile {
    private String id;
    private String firstname;
    private String middleName;
    private String lastname;

    private URL profileUrl;
    @Size(min = 11, max = 13)
    private String phoneNumber;
    private List<String> skills;
    private LocationDto location;
    private String email;
    private boolean emailEditable;
    private boolean emailIsVerified;
    private boolean phoneIsVerified;
    private LocalDateTime createdAt;
}
