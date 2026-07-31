package com.backend.givr.volunteer.mappings;

import com.backend.givr.organization.dtos.LocationDto;
import com.backend.givr.shared.Location;
import com.backend.givr.shared.mapper.SharedMapper;
import com.backend.givr.volunteer.dtos.CreateVolunteerRequestDto;
import com.backend.givr.volunteer.dtos.UpdateVolunteerDto;
import com.backend.givr.volunteer.dtos.VolunteerDto;
import com.backend.givr.volunteer.dtos.VolunteerProfile;
import com.backend.givr.volunteer.entity.Volunteer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = {SharedMapper.class})
public interface VolunteerMapper {

    @Mapping(source = "phone", target = "phoneNumber")
    Volunteer toVolunteer(CreateVolunteerRequestDto dto);
    void updateVolunteer(UpdateVolunteerDto updatedVolunteer, @MappingTarget Volunteer volunteer);

    VolunteerProfile toProfile(Volunteer volunteer);

    VolunteerDto toVolunteerDto (Volunteer volunteer);
    Location toLocation (LocationDto locationDto);
}
