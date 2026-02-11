package com.backend.givr.organization.service;

import com.backend.givr.organization.dtos.ProjectRequestDto;
import com.backend.givr.organization.dtos.ProjectResponseDto;
import com.backend.givr.organization.entity.Organization;
import com.backend.givr.organization.entity.Project;
import com.backend.givr.organization.repo.ProjectRepo;
import com.backend.givr.shared.Location;
import com.backend.givr.shared.enums.ProjectStatus;
import com.backend.givr.shared.exceptions.IllegalOperationException;
import com.backend.givr.shared.exceptions.InconsistentProjectDatesException;
import com.backend.givr.shared.mapper.ProjectMapper;
import com.backend.givr.shared.service.LocationService;
import com.backend.givr.shared.service.SkillService;
import com.backend.givr.volunteer.entity.Volunteer;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.Comparator;
import java.util.List;

@Service
public class ProjectService {
    @Autowired
    private ProjectRepo repo;
    @Autowired
    private ProjectMapper mapper;
    @Autowired
    private SkillService skillService;
    @Autowired
    private LocationService locationService;

    public List<ProjectResponseDto> getAllProjects(){
        return mapper.toDtos(repo.findAll());
    }

    public List<ProjectResponseDto> getAllProjectsForVolunteer(){
        Instant now = Instant.now();
        List<Project> result = repo.findAllByStatus(ProjectStatus.OPEN).stream()
                .filter(project -> {
                    LocalDateTime endOfDay = project.getDeadline().atTime(23, 59, 59);
                    return endOfDay.isAfter(LocalDateTime.now());
                })
                .sorted(Comparator.comparing(Project::getCreatedAt))
                .toList();
        return mapper.toDtos(result);
    }

    public List<Project> getProjectByOrganizationAndStatus(Organization organization, ProjectStatus status){
        return repo.findAllByOrganizationAndStatus(organization, status);
    }

    public List<Project> getOrganizationProjects(Organization organization){
        return repo.findAllByOrganizationOrderByCreatedAtAsc(organization);
    }

    private void handleProject(Project project, ProjectRequestDto projectRequestDto){
        // Verify application date are valid
        if(!projectDatesValid(project))
            throw new InconsistentProjectDatesException("Cannot start or end a project before current date. Application deadline must be before the project's start date and the project's start date must be before its end date");

        project.setStatus(ProjectStatus.DRAFT);
        Location savedLocation = locationService.createLocation(projectRequestDto.getLocation());
        project.setLocation(savedLocation);

        var updatedSkills = skillService.updateSkills(projectRequestDto.getRequiredSkills());
        project.setRequiredSkills(updatedSkills);
    }

    public Project createProject(ProjectRequestDto projectRequestDto, Organization organization){
        Project project = mapper.toProject(projectRequestDto);
        handleProject(project, projectRequestDto);
        project.setOrganization(organization);
        return repo.save(project);
    }

    @Transactional
    public Project updateProject(Long projectId, ProjectRequestDto projectRequestDto){
        Project project = findProjectById(projectId);
        mapper.updateProject(projectRequestDto, project);
        handleProject(project, projectRequestDto);

        return repo.save(project);
    }
    private boolean projectDatesValid(Project project){
        var startDateBeforeNow = project.getStartDate().isAfter(LocalDate.now(ZoneId.of("Africa/Lagos")));
        var endDateBeforeNow = project.getEndDate().isAfter(LocalDate.now(ZoneId.of("Africa/Lagos")));
        var deadlineBeforeStart = project.getDeadline().isBefore(project.getStartDate());
        var startBeforeEndDate = project.getStartDate().isBefore(project.getEndDate());

        return startBeforeEndDate && endDateBeforeNow && deadlineBeforeStart && startDateBeforeNow;
    }
    public  Project findProjectById(Long projectId){
        return repo.findById(projectId).orElseThrow(()-> new EntityNotFoundException(String.format("Project with id [%s] does not exist", projectId)));
    }

    public void save(Project project){
        repo.save(project);
    }


    public void deleteProject(Long projectId, Organization organization) {
        Project project = repo.findByProjectIdAndOrganization(projectId, organization).orElseThrow(()->new EntityNotFoundException("Project not found"));
        if(project.getStatus() != ProjectStatus.DRAFT && project.getStatus() != ProjectStatus.OPEN)
            throw new IllegalOperationException("Only DRAFT or OPEN projects can be deleted");
        repo.delete(project);
    }

    @Scheduled(cron = "0 0 1 * * *", zone = "Africa/Lagos")
    @Transactional
    public void updateProjectStatusOnDeadline(){
        LocalDate today = LocalDate.now(ZoneId.of("Africa/Lagos"));
        List<Project> projects = repo.findExpiredProjects(today);

        projects.forEach(project -> {
            if(project.shouldClose(today.atStartOfDay()))
                project.closeApplication();
        });
    }

    public List<Project> getVolunteerRecommendedProjects(Volunteer volunteer, ProjectStatus status){
        return repo.findProjectsWithAnyMatchingSkill(volunteer, volunteer.getLocation().getState(),status);
    }
}
