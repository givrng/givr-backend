package com.backend.givr.organization.mappings;

import com.backend.givr.organization.dtos.*;
import com.backend.givr.organization.entity.Organization;
import com.backend.givr.shared.entity.OrganizationVerificationSession;
import com.backend.givr.organization.entity.Project;
import com.backend.givr.shared.entity.Location;
import com.backend.givr.shared.entity.Skill;
import com.backend.givr.shared.mapper.SkillMapper;
import com.backend.givr.volunteer.mappings.VolunteerMapper;
import org.mapstruct.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {VolunteerMapper.class, SkillMapper.class, }, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface OrganizationMapper {

    Organization toOrganization(CreateOrganizationDto organizationDto);

    @AfterMapping
    default void updateVolunteerAndOrganization(Project project, @MappingTarget ProjectResponseDto projectDto){
        projectDto.setTotalApplicants(project.getVolunteerCount());
    }

    @Mapping(target = "name", source = "organizationName")
    OrganizationDto toOrganizationDto (Organization organization);

    @Mapping(target = "name", source = "claimedOrgName")
    @Mapping(target = "cacRegNumber", source = "claimedCACRegNumber")
    @Mapping(target = "location", source = "claimedLocation")
    @Mapping(target = "address", ignore = true)
    OrganizationDto toOrganizationDto (OrganizationVerificationSession verificationSession);

    @AfterMapping
    default void updateOrganizationDtoAddress(OrganizationVerificationSession verificationSession, @MappingTarget OrganizationDto organizationDto){
        organizationDto.setAddress(verificationSession.getClaimedAddress().address());
        LocationDto locationDto = new LocationDto();
        locationDto.setLga(verificationSession.getClaimedAddress().LGA());
        locationDto.setState(verificationSession.getClaimedAddress().state());
        organizationDto.setLocation(locationDto);
    }

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
    @Mapping(target = "organizationType", ignore = true)
    @Mapping(target = "location", ignore = true)
    @Mapping(target = "cacRegNumber", ignore = true)
    @Mapping(target = "organizationName", ignore = true)
    @Mapping(target = "address", ignore = true)
    void updateOrganization(OrganizationUpdateDto organizationDto, @MappingTarget Organization organization);

    @AfterMapping
    default void updateOrganizationType(OrganizationUpdateDto organizationUpdateDto, @MappingTarget Organization organization){
        if(organizationUpdateDto.getCategory() != null)
            organization.setOrganizationType(organizationUpdateDto.getCategory().getFirst());
    }

}
