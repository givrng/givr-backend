package com.backend.givr.organization.service;

import com.backend.givr.organization.dtos.ProjectDto;
import com.backend.givr.organization.entity.Organization;
import com.backend.givr.organization.entity.Project;
import com.backend.givr.organization.mappings.OrganizationMapper;
import com.backend.givr.organization.repo.ProjectRepo;
import com.backend.givr.shared.enums.ProjectStatus;
import com.backend.givr.shared.exceptions.InconsistentProjectDatesException;
import com.backend.givr.shared.service.LocationService;
import com.backend.givr.shared.service.SkillService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.List;

@Service
public class ProjectService {
    @Autowired
    private ProjectRepo repo;
    @Autowired
    private OrganizationMapper mapper;
    @Autowired
    private SkillService skillService;
    @Autowired
    private LocationService locationService;

    public List<ProjectDto> getAllProjects(){
        return mapper.toDtos(repo.findAll());
    }

    public List<ProjectDto> getAllProjectsForVolunteer(){
        List<Project> result = repo.findAll().stream().filter(project -> project.getStartDate().before(Date.from(Instant.now()))).toList();
        return mapper.toDtos(result);
    }

    public List<Project> getOrganizationProjects(Organization organization){
        return repo.findAllByOrganization(organization);
    }
    public Project createProject(ProjectDto projectDto, Organization organization){
        Project project = mapper.toProject(projectDto);

        // Verify application date are valid
        if(!projectDatesValid(project))
            throw new InconsistentProjectDatesException("Cannot start or end a project before current date. Application deadline must be before the project's start date and the project's start date must be before its end date");

        project.setStatus(ProjectStatus.DRAFT);
        project.setLocation(locationService.createLocation(project.getLocation()));

        var updatedSkills = skillService.updateSkills(projectDto.getRequiredSkills());
        project.setRequiredSkills(updatedSkills);
        project.setOrganization(organization);
        return repo.save(project);
    }

    private boolean projectDatesValid(Project project){
        var startDateBeforeNow = project.getStartDate().before(Date.from(Instant.now()));
        var endDateBeforeNow = project.getEndDate().before(Date.from(Instant.now()));
        var deadlineBeforeStart = project.getDeadline().before(project.getStartDate());
        var startBeforeEndDate = project.getStartDate().before(project.getEndDate());

        return startBeforeEndDate && endDateBeforeNow && deadlineBeforeStart && startDateBeforeNow;
    }
    public  Project findProjectById(Long projectId){
        return repo.findById(projectId).orElseThrow(()-> new EntityNotFoundException(String.format("Project with id [%s] does not exist", projectId)));
    }

    public void save(Project project){
        repo.save(project);
    }
}
