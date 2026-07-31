package com.backend.givr.volunteer.dtos;

import com.backend.givr.shared.Location;
import jakarta.validation.constraints.Size;

public class VolunteerProfile {
    private String volunteerId;
    private String firstname;
    private String middleName;
    private String lastname;

    @Size(min = 11, max = 13)
    private String phoneNumber;

    private Location location;
}
