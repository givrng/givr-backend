package com.backend.givr.volunteer.dtos;

import com.backend.givr.organization.dtos.LocationDto;
import com.backend.givr.shared.dtos.SkillDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class VolunteerDto {
    private String volunteerId;
    private String firstname;
    private String middleName;
    private String lastname;
    private String phoneNumber;
    private String email;
    private LocationDto location;
    private List<SkillDto> skills;

}
