package com.backend.givr.organization.service;

import com.backend.givr.organization.dtos.ProjectRequestDto;
import com.backend.givr.organization.dtos.ProjectResponseDto;
import com.backend.givr.organization.entity.Organization;
import com.backend.givr.organization.entity.Project;
import com.backend.givr.organization.repo.ProjectRepo;
import com.backend.givr.organization.security.ProjectServiceWorker;
import com.backend.givr.shared.email.EmailService;
import com.backend.givr.shared.entity.Location;
import com.backend.givr.shared.enums.ProjectStatus;
import com.backend.givr.shared.exceptions.IllegalOperationException;
import com.backend.givr.shared.exceptions.InconsistentProjectDatesException;
import com.backend.givr.shared.mapper.ProjectMapper;
import com.backend.givr.shared.service.*;
import com.backend.givr.volunteer.dtos.ProjectViewResponse;
import com.backend.givr.volunteer.entity.Volunteer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.time.*;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

@Service
@Slf4j
public class ProjectService {
    @Autowired
    private ProjectRepo repo;
    @Autowired
    private ProjectMapper mapper;
    @Autowired
    private SkillService skillService;
    @Autowired
    private EmailService emailService;
    @Autowired
    private LocationService locationService;
    @Autowired
    private EntityManager manager;
    @Autowired
    private ProjectServiceWorker worker;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private Logger logger = LoggerFactory.getLogger(ProjectService.class);

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

    public Project getProject(Long projectId){
        return repo.findById(projectId).orElseThrow();
    }

    public String getProjectTitle(Long projectId){
        return Mono.just(Objects.requireNonNull(redisTemplate.opsForValue()
                        .get("project:" + projectId)))
                .cast(String.class)
                .switchIfEmpty(Mono.just(
                        getProject(projectId)
                ).map(Project::getTitle)
                    .doOnNext(title->{
                        redisTemplate.opsForValue().set("project:"+projectId, title, Duration.ofHours(4));
                    })
                )
                .block();
    }

    public List<Project> getOrganizationProjects(Organization organization){
        return repo.findAllByOrganizationOrderByCreatedAtAsc(organization);
    }

    private void handleProject(Project project, ProjectRequestDto projectRequestDto){
        // Verify application date are valid
        if(!projectDatesValid(project))
            throw new InconsistentProjectDatesException("Project dates are invalid. Start and end dates cannot be in the past, the application deadline must be before the start date, and the start date must be before the end date.");
        Location savedLocation = locationService.createLocation(projectRequestDto.getLocation());
        project.setLocation(savedLocation);

        var updatedSkills = skillService.updateSkills(projectRequestDto.getRequiredSkills());
        project.setRequiredSkills(updatedSkills);
    }

    public Project createProject(ProjectRequestDto projectRequestDto, Organization organization){
        Project project = mapper.toProject(projectRequestDto);
        handleProject(project, projectRequestDto);
        project.setOrganization(organization);
        project.setStatus(ProjectStatus.DRAFT);
        project.setBroadcastEnabled(repo.count() <= 3);
        Project savedProject = repo.save(project);
        worker.createProjectSegment(savedProject);
        worker.createProjectCard(savedProject);
        return  project;
    }


    @Transactional
    public Project updateProject(Long projectId, ProjectRequestDto projectRequestDto){
        Project project = findProjectById(projectId);
        mapper.updateProject(projectRequestDto, project);

        if(!project.getTitle().equals(projectRequestDto.getTitle()) || !project.getDescription().equals(projectRequestDto.getDescription()))
            worker.createProjectCard(project);

        handleProject(project, projectRequestDto);
        if(project.getStartDate().isBefore(LocalDate.parse(projectRequestDto.getStartDate())) ) {
            project.setStatus(ProjectStatus.OPEN);
            worker.sendProjectListing(project);
        }
        return project;
    }
    private boolean projectDatesValid(Project project){
        var startDateAfterNow = project.getStartDate().isAfter(LocalDate.now(ZoneId.of("Africa/Lagos")));
        var endDateAfterNow = project.getEndDate().isAfter(LocalDate.now(ZoneId.of("Africa/Lagos")));
        var deadlineBeforeStart = project.getDeadline().isBefore(project.getStartDate());
        var deadlineEqualsStart = project.getDeadline().isEqual(project.getStartDate());
        var startBeforeEndDate = project.getStartDate().isBefore(project.getEndDate());
        var startEqualsEndDate = project.getStartDate().isEqual(project.getEndDate());
        if(startEqualsEndDate)
            project.setReviewable(true);
        return (startBeforeEndDate || startEqualsEndDate) && endDateAfterNow && (deadlineBeforeStart || deadlineEqualsStart) && startDateAfterNow;
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
        List<Project> reviewableProjects = repo.findClosingProjects(today.plusDays(1));
        projects.forEach(project -> {
            if(project.shouldClose(today.atStartOfDay()))
                project.closeApplication();
        });

        reviewableProjects.forEach(project -> project.setReviewable(true));
    }


    public List<Project> getVolunteerRecommendedProjects(Volunteer volunteer, ProjectStatus status){
        return repo.findProjectsWithAnyMatchingSkill(volunteer, volunteer.getLocation().getState(),status);
    }

    public String shareProject(Long projectId) throws ExecutionException, InterruptedException {
        Project project = getProject(projectId);

        if(StringUtils.hasLength(project.getShareableLink())){
            return project.getShareableLink();
        }else{
             return worker.createProjectCard(project).get();
        }
    }

    public List<ProjectViewResponse> getActiveProjectsByOrganization(String orgId){
        Organization organization = manager.getReference(Organization.class, orgId);
        return mapper.toProjectViewResponses(repo.findAllByOrganizationAndStatusNot(organization, ProjectStatus.DRAFT));
    }

    public List<Project> getAllProjectsByStatus(ProjectStatus status){
        return repo.findAllByStatus(status);
    }
}
