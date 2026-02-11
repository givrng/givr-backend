package com.backend.givr.organization.mappings;

import com.backend.givr.organization.dtos.*;
import com.backend.givr.organization.entity.Organization;
import com.backend.givr.organization.entity.Project;
import com.backend.givr.organization.entity.ProjectApplication;
import com.backend.givr.shared.Location;
import com.backend.givr.shared.Skill;
import com.backend.givr.shared.mapper.SkillMapper;
import com.backend.givr.volunteer.mappings.VolunteerMapper;
import org.mapstruct.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Mapper(componentModel = "spring", uses = {VolunteerMapper.class, SkillMapper.class}, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface OrganizationMapper {

    Organization toOrganization(CreateOrganizationDto organizationDto);

    @AfterMapping
    default void updateVolunteerAndOrganization(Project project, @MappingTarget ProjectResponseDto projectDto){
        projectDto.setTotalApplicants(project.getVolunteerCount());
    }

    @Mapping(target = "id", source = "organizationId")
    @Mapping(target = "name", source = "organizationName")
    OrganizationDto toOrganizationDto (Organization organization);

    @AfterMapping
    default void updateActiveProjectCount(Organization organization, @MappingTarget OrganizationDto organizationDto){
        organizationDto.setNumOfActiveProjects(organization.getNumOfActiveProjects());
        List<String> organizationType = organization.getOrganizationType() == null? List.of("") : List.of(organization.getOrganizationType());
        organizationDto.setCategory(organizationType);
    }

    List<OrganizationDto> toOrganizationDtoList (List<Organization> organizations);

    LocationDto toLocationDto (Location location);


    default Date toDate(String date)throws ParseException{
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return format.parse(date);
    }
    default Set<Skill> toSet(List<Skill> skills){
        return Set.copyOf(skills);
    }


    OrganizationContactDto toOrganizationContact(Organization organization);

    // name -> organizationName;
    // category -> organizationType;
    @Mapping(source = "name", target = "organizationName")
    @Mapping(target = "organizationType", ignore = true)
    @Mapping(target = "location", ignore = true)
    void updateOrganization(OrganizationUpdateDto organizationDto, @MappingTarget Organization organization);

    @AfterMapping
    default void updateOrganizationType(OrganizationUpdateDto organizationUpdateDto, @MappingTarget Organization organization){
        if(organizationUpdateDto.getCategory() != null)
            organization.setOrganizationType(organizationUpdateDto.getCategory().getFirst());
    }
}
