package com.backend.givr.volunteer.dtos;

import com.backend.givr.organization.dtos.LocationDto;
import com.backend.givr.shared.Location;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.net.URL;
import java.util.List;

@NoArgsConstructor
@Getter
public class UpdateVolunteerDto {
    private String firstname;
    private String middleName;
    private String lastname;

    private URL profileUrl;
    @Size(min = 11, max = 13)
    private String phoneNumber;
    private List<String> skills;
    private LocationDto location;
    private String email;
}
