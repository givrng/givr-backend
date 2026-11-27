package com.backend.givr.shared.service;

import com.backend.givr.shared.dtos.ProjectRequestDto;
import com.backend.givr.shared.dtos.ProjectResponseDto;
import com.backend.givr.organization.entity.Organization;
import com.backend.givr.shared.entity.Creator;
import com.backend.givr.organization.repo.ProjectRepo;
import com.backend.givr.organization.security.ProjectServiceWorker;
import com.backend.givr.shared.email.EmailService;
import com.backend.givr.shared.entity.Location;
import com.backend.givr.shared.entity.projects.Initiative;
import com.backend.givr.shared.entity.projects.Project;
import com.backend.givr.shared.enums.ProjectStatus;
import com.backend.givr.shared.exceptions.IllegalOperationException;
import com.backend.givr.shared.exceptions.InconsistentProjectDatesException;
import com.backend.givr.shared.mapper.ProjectMapper;
import com.backend.givr.shared.repo.ResourceRepo;
import com.backend.givr.shared.repo.InitiativeRepo;
import com.backend.givr.volunteer.dtos.ProjectViewResponse;
import com.backend.givr.volunteer.entity.Volunteer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.web.PagedModel;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.*;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
@Slf4j
public class ProjectService {
    @Autowired
    private ProjectRepo repo;
    @Autowired
    private InitiativeRepo initiativeRepo;
    @Autowired
    private ResourceRepo commodityRepo;

    @Autowired
    private ProjectMapper mapper;
    @Autowired
    private CreatorService creatorService;
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

    public List<ProjectResponseDto> getAllProjects(){
        return mapper.toDtos(repo.findAll());
    }

    public PagedModel<ProjectResponseDto> getAllProjectsForVolunteer(PageRequest pageRequest){
        pageRequest = pageRequest.withSort(Sort.by("deadline").ascending());
        LocalDate now = LocalDate.from(LocalDate.now(ZoneId.of("Africa/Lagos"))
                .atTime(LocalTime.now(ZoneId.of("Africa/Lagos"))));
        Page<Project> result = repo.findAllByStatusAndDeadlineAfter(ProjectStatus.OPEN, now, pageRequest);

        PageImpl<ProjectResponseDto> responseDtos = new PageImpl<>(mapper.toDtos(result.getContent()), result.getPageable(), result.getTotalElements());
        return new PagedModel<>(responseDtos);
    }

    public List<Project> getProjectByOrganizationAndStatus(Organization organization, ProjectStatus status){
        return repo.findAllByOrganizationAndStatus(organization, status);
    }

    public Project getProject(Long projectId){
        return repo.findByProjectIdAndStatusNot(projectId, ProjectStatus.DRAFT).orElseThrow(()->new EntityNotFoundException("Project does not exist"));
    }

    public String getProjectTitle(Long projectId) {

        String cached = (String) redisTemplate.opsForValue()
                .get("project:" + projectId);

        if (cached != null) {
            return cached;
        }

        String title = getProject(projectId).getTitle();

        redisTemplate.opsForValue().set(
                "project:" + projectId,
                title,
                Duration.ofHours(4)
        );

        return title;
    }

    public List<Project> getOrganizationProjects(Organization organization){
        return repo.findAllByOrganizationOrderByCreatedAtAsc(organization);
    }

    protected void handleProject(Project project, ProjectRequestDto projectRequestDto){
        // Verify application date are valid
        if(!projectDatesValid(project))
            throw new InconsistentProjectDatesException("BaseProject dates are invalid. Start and end dates cannot be in the past, the application deadline must be before the start date, and the start date must be before the end date.");
        Location savedLocation = locationService.createLocation(projectRequestDto.getLocation());

        if(savedLocation != null)
            project.setLocation(savedLocation);

        var updatedSkills = skillService.updateSkills(projectRequestDto.getRequiredSkills());
        if(updatedSkills!=null)
            project.setRequiredSkills(updatedSkills);
    }

    public void createProject(ProjectRequestDto projectRequestDto, Organization organization){
        Project project = mapper.toProject(projectRequestDto);
        handleProject(project, projectRequestDto);

        Creator creator = creatorService.createOrReturnCreator(organization);
        project.setCreator(creator);
        project.setOrganization(organization);
        project.setStatus(ProjectStatus.DRAFT);
        project.setOrganizerName(organization.getOrganizationName());
        Project savedProject = repo.save(project);
        worker.createProjectCard(savedProject);
    }


    @Transactional
    protected Project updateProject(Project project, ProjectRequestDto projectRequestDto){
        boolean sendNewListing = false;
        var updatedTitle = projectRequestDto.getTitle();
        var updatedDescription = projectRequestDto.getDescription();
        var updatedStartDate = projectRequestDto.getStartDate();
        if((updatedTitle != null && !project.getTitle().equals(projectRequestDto.getTitle())) || (updatedDescription!=null &&!project.getDescription().equals(projectRequestDto.getDescription())))
            worker.createProjectCard(project);

        if(updatedStartDate != null && project.getStartDate().isBefore(LocalDate.parse(projectRequestDto.getStartDate())) ) {
            project.setStatus(ProjectStatus.OPEN);
            sendNewListing = true;
        }
        mapper.updateProject(projectRequestDto, project);
        handleProject(project, projectRequestDto);

        if(sendNewListing)
            worker.sendProjectListing(project).subscribe();

        return project;
    }

    public Project orgUpdateProject(Long projectId, Organization organization, ProjectRequestDto projectRequestDto){
        Project project = findProjectByIdAndOrganization(projectId, organization);
        var updatedProject =  updateProject(project, projectRequestDto);

        if(updatedProject instanceof Project p)
            return p;
        else
            return null;
    }

    private boolean projectDatesValid(Project project) {
        LocalDate today = LocalDate.now(ZoneId.of("Africa/Lagos"));

        LocalDate startDate = project.getStartDate();
        LocalDate endDate = project.getEndDate();
        LocalDate deadline = project.getDeadline();

        boolean startDateValid = startDate != null && startDate.isAfter(today);
        boolean endDateValid = endDate!= null && endDate.isAfter(today);
        boolean dateRangeValid = startDate !=null && endDate !=null && !startDate.isAfter(endDate);
        boolean deadlineValid = deadline !=null && startDate!=null && !deadline.isAfter(startDate);

        if (startDate !=null && endDate !=null && startDate.isEqual(endDate))
            project.setReviewable(true);

        return startDateValid
                && endDateValid
                && dateRangeValid
                && deadlineValid;
    }

    public Project findProjectById(Long projectId){
        return repo.findById(projectId).orElseThrow(()-> new EntityNotFoundException(String.format("BaseProject with id [%s] does not exist", projectId)));
    }
    public Project findProjectByIdAndOrganization(Long projectId, Organization organization){
        return repo.findByProjectIdAndOrganization(projectId, organization).orElseThrow(()-> new EntityNotFoundException(String.format("BaseProject with id [%s] does not exist", projectId)));
    }

    public void save(Project project){
        repo.save(project);
    }


    public void deleteProject(Long projectId, Organization organization) {
        Project project = repo.findByProjectIdAndOrganization(projectId, organization).orElseThrow(()->new EntityNotFoundException("BaseProject not found"));
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
