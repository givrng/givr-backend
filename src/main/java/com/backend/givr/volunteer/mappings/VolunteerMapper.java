package com.backend.givr.volunteer.mappings;

import com.backend.givr.organization.dtos.LocationDto;
import com.backend.givr.shared.entity.Location;
import com.backend.givr.shared.entity.Skill;
import com.backend.givr.shared.mapper.SkillMapper;
import com.backend.givr.volunteer.dtos.CreateVolunteerRequestDto;
import com.backend.givr.volunteer.dtos.UpdateVolunteerDto;
import com.backend.givr.volunteer.dtos.VolunteerDto;
import com.backend.givr.volunteer.dtos.VolunteerProfile;
import com.backend.givr.volunteer.entity.Volunteer;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {SkillMapper.class}, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface VolunteerMapper {

    @Mapping(source = "phone", target = "phoneNumber")
    Volunteer toVolunteer(CreateVolunteerRequestDto dto);

    @Mapping(target = "skills", ignore = true)
    @Mapping(target = "location", ignore = true)
    void updateVolunteer(UpdateVolunteerDto updatedVolunteer, @MappingTarget Volunteer volunteer);


    @Mapping(target = "skills", ignore = true)
    @Mapping(target = "id", source = "volunteerId")
    VolunteerProfile toProfile(Volunteer volunteer);

    @AfterMapping
    default  void updateProfile(Volunteer volunteer, @MappingTarget VolunteerProfile profile){
        profile.setSkills(volunteer.getSkills().stream().map(Skill::getName).toList());
    }

    VolunteerDto toVolunteerDto (Volunteer volunteer);
    Location toLocation (LocationDto locationDto);

}
