package com.backend.givr.volunteer.dtos;

import com.backend.givr.organization.dtos.LocationDto;
import com.backend.givr.shared.Location;
import com.backend.givr.shared.SkillDto;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class VolunteerDto {
    private String volunteerId;
    private String firstname;
    private String middleName;
    private String lastname;
    private String phoneNumber;

    private LocationDto location;
    private List<SkillDto> skill;

}
