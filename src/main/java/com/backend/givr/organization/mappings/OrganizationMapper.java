package com.backend.givr.organization.mappings;

import com.backend.givr.organization.dtos.*;
import com.backend.givr.organization.entity.Organization;
import com.backend.givr.organization.entity.Project;
import com.backend.givr.organization.entity.ProjectApplication;
import com.backend.givr.shared.Location;
import com.backend.givr.shared.Skill;
import com.backend.givr.shared.mapper.SharedMapper;
import com.backend.givr.volunteer.mappings.VolunteerMapper;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.List;
import java.util.Locale;

@Mapper(componentModel = "spring", uses = {VolunteerMapper.class, SharedMapper.class})
public interface OrganizationMapper {

    Organization toOrganization(CreateOrganizationDto organizationDto);

    @Mapping(target = "requiredSkills",ignore = true)
    @Mapping(target = "startDate", ignore = true)
    @Mapping(target = "endDate", ignore = true)
    @Mapping(target = "deadline", ignore = true)
    @Mapping(target = "category", ignore = true)
    Project toProject (ProjectDto projectDto);

    @AfterMapping
    default void updateCategory(ProjectDto projectDto, @MappingTarget Project project){
        project.setCategory(projectDto.getCategories().getFirst());
    }

    @Mapping(target = "requiredSkills", ignore = true)
    @Mapping(target = "id", source = "projectId")
    @Mapping(source = "deadline", target = "applicationDeadline")
    @Mapping(target = "categories", ignore = true)
    ProjectDto toProjectDTO (Project project);

    @AfterMapping
    default void updateCategory(Project project, @MappingTarget ProjectDto projectDto){
        projectDto.setCategories(List.of(project.getCategory()));
    }

    @AfterMapping
    default void mapDates(ProjectDto projectDto, @MappingTarget Project project) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        try{
            project.setStartDate(formatter.parse(projectDto.getStartDate()));
            project.setDeadline(formatter.parse(projectDto.getApplicationDeadline()));
            project.setEndDate(formatter.parse(projectDto.getEndDate()));
        }catch (ParseException e){
            System.out.println(e.getLocalizedMessage());
        }
    }

    @AfterMapping
    default void mapSkills(Project project, @MappingTarget ProjectDto dto){
        dto.setRequiredSkills(project.getRequiredSkills().stream().map(Skill::toString).toList());
    }

    @Mapping(target = "id", source = "organizationId")
    @Mapping(target = "name", source = "organizationName")
    @Mapping(target = "category", source = "organizationType")
    OrganizationDto toOrganizationDto (Organization organization);

    List<OrganizationDto> toOrganizationDtoList (List<Organization> organizations);
    List<ProjectDto> toDtos(List<Project> projects);

    LocationDto toLocationDto (Location location);

    ProjectApplicationDto toApplicationDto(ProjectApplication application);
    List<ProjectApplicationDto> toApplicationsDto(List<ProjectApplication> application);
}
