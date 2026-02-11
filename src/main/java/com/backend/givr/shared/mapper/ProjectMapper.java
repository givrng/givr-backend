package com.backend.givr.shared.mapper;

import com.backend.givr.organization.dtos.ProjectApplicationDto;
import com.backend.givr.organization.dtos.ProjectRequestDto;
import com.backend.givr.organization.dtos.ProjectResponseDto;
import com.backend.givr.organization.entity.Participation;
import com.backend.givr.organization.entity.Project;
import com.backend.givr.organization.entity.ProjectApplication;
import com.backend.givr.organization.mappings.OrganizationMapper;
import com.backend.givr.shared.dtos.ParticipationDto;
import com.backend.givr.shared.Skill;
import org.mapstruct.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {OrganizationMapper.class})
public interface ProjectMapper {

    @Mapping(target = "requiredSkills",ignore = true)
    @Mapping(target = "startDate", ignore = true)
    @Mapping(target = "endDate", ignore = true)
    @Mapping(target = "deadline", ignore = true)
    @Mapping(target = "category", ignore = true)

    Project toProject (ProjectRequestDto projectRequestDto);

    @AfterMapping
    default void updateCategory(ProjectRequestDto projectRequestDto, @MappingTarget Project project){
        project.setCategory(projectRequestDto.getCategories().getFirst());
    }

    @AfterMapping
    default void mapDates(ProjectRequestDto projectRequestDto, @MappingTarget Project project) {
        try{

            project.setStartDate(LocalDate.parse(projectRequestDto.getStartDate()));
            project.setDeadline(LocalDate.parse(projectRequestDto.getApplicationDeadline()));
            project.setEndDate(LocalDate.parse(projectRequestDto.getEndDate()));

        }catch (DateTimeParseException e){
            System.out.println(e.getLocalizedMessage());
        }
    }


    @Mapping(target = "requiredSkills", ignore = true)
    @Mapping(target = "id", source = "projectId")
    @Mapping(source = "deadline", target = "applicationDeadline")
    @Mapping(target = "categories", ignore = true)
    ProjectResponseDto toProjectDto (Project project);

    @AfterMapping
    default void updateVolunteerAndOrganization(Project project, @MappingTarget ProjectResponseDto projectDto){
        projectDto.setTotalApplicants(project.getVolunteerCount());
        projectDto.setCategories(List.of(project.getCategory()));
    }

    @AfterMapping
    default void mapSkills(Project project, @MappingTarget ProjectResponseDto dto){
        dto.setRequiredSkills(project.getRequiredSkills().stream().map(Skill::toString).collect(Collectors.toSet()));
    }

    List<ProjectResponseDto> toDtos(List<Project> projects);

    ProjectApplicationDto toApplicationDto(ProjectApplication application);

    List<ProjectApplicationDto> toApplicationsDto(List<ProjectApplication> application);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)

    @Mapping(target = "requiredSkills", ignore = true)
    @Mapping(target = "location", ignore = true)
    void updateProject(ProjectRequestDto projectRequestDto, @MappingTarget Project project);

    @Mapping(target = "status", source = "participationStatus")
    ParticipationDto toParticipationDto(Participation participation);

    List<ParticipationDto> toParticipationDto(List<Participation> participationList);
}
