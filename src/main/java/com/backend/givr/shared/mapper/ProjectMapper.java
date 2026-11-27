package com.backend.givr.shared.mapper;

import com.backend.givr.organization.dtos.ProjectApplicationDto;
import com.backend.givr.shared.dtos.*;
import com.backend.givr.organization.entity.Category;
import com.backend.givr.shared.entity.Creator;
import com.backend.givr.shared.entity.Participation;
import com.backend.givr.shared.entity.projects.*;
import com.backend.givr.shared.entity.ProjectApplication;
import com.backend.givr.organization.mappings.OrganizationMapper;
import com.backend.givr.shared.entity.Skill;
import com.backend.givr.shared.enums.DonationStatus;
import com.backend.givr.shared.enums.ProjectType;
import com.backend.givr.volunteer.dtos.ProjectResponseDTOv;
import com.backend.givr.volunteer.dtos.ProjectViewResponse;
import com.backend.givr.volunteer.mappings.VolunteerMapper;
import org.mapstruct.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {OrganizationMapper.class, VolunteerMapper.class})
public interface ProjectMapper {

    @Mapping(target = "requiredSkills",ignore = true)
    @Mapping(target = "startDate", ignore = true)
    @Mapping(target = "endDate", ignore = true)
    @Mapping(target = "deadline", ignore = true)
    Project toProject (ProjectRequestDto projectRequestDto);

    @Mapping(target = "requiredSkills",ignore = true)
    @Mapping(target = "startDate", ignore = true)
    @Mapping(target = "endDate", ignore = true)
    @Mapping(target = "deadline", ignore = true)
    @Mapping(target = "media", ignore = true)
    Initiative toInitiative(InitiativeRequestDto initiativeRequestDto);

    @Mapping(source = "url", target = "mediaUrl")
    MediaDto toMediaDto(Media media);

    @Mapping(source = "quantity", target = "quantityDonated")
    DonationDto toDonationDto(Donation donation);

    @AfterMapping
    default void updateDonationDto(Donation donation, @MappingTarget DonationDto donationDto){
        donationDto.setInitiative(donation.getItem().getInitiative().toMinimalDto());
        donationDto.setItemName(donation.getItem().getResource().getName());

        DonationStatus donationStatus = donation.getStatus();

        if(!(donationStatus.equals(DonationStatus.Promised_Accepted) || donationStatus.equals(DonationStatus.Promise_Fulfilled))){
            donationDto.setDonor(null);
        }
        
    }

    List<DonationDto> toDonationDtos(List<Donation> donations);


    default List<Category> toCategories (List<String> cats){
        return cats == null? null: cats.stream().map(Category::new).toList();
    }

    default List<String> toCategoriesString(List<Category> categoryList){
        return categoryList.stream().map(Category::getCategory).toList();
    }

    @AfterMapping
    default void mapDates(ProjectRequestDto projectRequestDto, @MappingTarget Project project) {
        try{
            if(projectRequestDto.getStartDate() !=null)
                project.setStartDate(LocalDate.parse(projectRequestDto.getStartDate()));
            if(projectRequestDto.getApplicationDeadline()!=null)
                project.setDeadline(LocalDate.parse(projectRequestDto.getApplicationDeadline()));
            if(projectRequestDto.getEndDate() != null)
                project.setEndDate(LocalDate.parse(projectRequestDto.getEndDate()));
        }catch (DateTimeParseException e){
            System.out.println(e.getLocalizedMessage());
        }
    }

    @Mapping(target = "requiredSkills", ignore = true)
    @Mapping(target = "id", source = "projectId")
    @Mapping(source = "deadline", target = "applicationDeadline")
    ProjectResponseDto toProjectDto (Project project);

    @Mapping(target = "requiredSkills", ignore = true)
    @Mapping(target = "id", source = "projectId")
    @Mapping(source = "deadline", target = "applicationDeadline")
    ProjectResponseDTOv toProjectDtov (Project project);

    @Mapping(target = "requiredSkills", ignore = true)
    @Mapping(target = "id", source = "projectId")
    @Mapping(source = "deadline", target = "applicationDeadline")
    @Mapping(source = "initiativeServices", target = "services")
    @Mapping(source = "initiativeItems", target = "items")
    InitiativeResponseDto toInitiative(Initiative project);

    @Mapping(target = "requiredSkills", ignore = true)
    @Mapping(target = "id", source = "projectId")
    ProjectViewResponse toProjectViewResponse (Project project);

    @AfterMapping
    default void updateProjectViewResponseSkills(Project project, @MappingTarget ProjectViewResponse projectViewResponse){
        projectViewResponse.setRequiredSkills(project.getRequiredSkills().stream().map(Skill::getName).collect(Collectors.toSet()));
    }
    List<ProjectViewResponse> toProjectViewResponses (List<Project> project);

    @AfterMapping
    default void updateVolunteerAndOrganization(Project project, @MappingTarget ProjectResponseDto projectDto){
        projectDto.setTotalParticipants(project.getVolunteerCount());
    }

    @AfterMapping
    default void updateProjectResponseV(Project project, @MappingTarget ProjectResponseDTOv projectDto){
        projectDto.setTotalParticipants(project.getVolunteerCount());
        projectDto.setRequiredSkills(project.getRequiredSkills().stream().map(Skill::toString).collect(Collectors.toSet()));
    }

    @AfterMapping
    default void mapSkills(Project project, @MappingTarget ProjectResponseDto dto){
        if(project.getRequiredSkills()==null)
            return;
        dto.setRequiredSkills(project.getRequiredSkills().stream().map(Skill::toString).collect(Collectors.toSet()));
        if(project instanceof Initiative)
            dto.setType(ProjectType.Individual);
        else
            dto.setType(ProjectType.Organization);
    }

    List<ProjectResponseDto> toDtos(List<Project> projects);

    List<ProjectResponseDTOv> toDtosv(List<Project> projects);

    ProjectApplicationDto toApplicationDto(ProjectApplication application);

    List<ProjectApplicationDto> toApplicationsDto(List<ProjectApplication> application);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "requiredSkills", ignore = true)
    @Mapping(target = "location", ignore = true)
    void updateProject(ProjectRequestDto projectRequestDto, @MappingTarget Project project);

    @Mapping(target = "status", source = "participationStatus")
    ParticipationDto toParticipationDto(Participation participation);

    @AfterMapping
    default void updateParticipationDto(Participation participation, @MappingTarget ParticipationDto participationDto){
        participationDto.setReason(participation.getProjectApplication().getApplicationReason());
    }

    List<ParticipationDto> toParticipationDto(List<Participation> participationList);

    RenderProjectDto toDto (Project project);

    @AfterMapping
    default void updateRenderProjectDto(Project project, @MappingTarget RenderProjectDto renderProjectDto){
        String name = project.getOrganizerName();
        renderProjectDto.setOrganizationName(name);
    }

    List<InitiativeResponseDto> toInitiativeReponses(List<Initiative> initiatives);

    @Mapping(source = "id", target = "resourceId")
    @Mapping(target = "resource", ignore = true)
    @Mapping(target = "quantity", source = "quantityRequested")
    InitiativeResourceDto toResourceDto(InitiativeItem resource);

    List<InitiativeResourceDto> toResourcesDto(List<InitiativeItem> items);
    @AfterMapping
    default void updateResource(InitiativeItem resource, @MappingTarget InitiativeResourceDto resourceDto){
        resourceDto.setResource(resource.getResource().getName());
    }

    @Mapping(target = "initiative", ignore = true)
    InitiativeItemDto toInitiativeItemDto(InitiativeItem item);

    @AfterMapping
    default void updateInitiativeItemDto(InitiativeItem item, @MappingTarget InitiativeItemDto dto){
        dto.setInitiative(item.getInitiative().toMinimalDto());
    }

    List<InitiativeItemDto> toInitiativeItemDtos(List<InitiativeItem> items);

    CreatorDto toCreatorDto(Creator creator);

}
