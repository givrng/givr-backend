package com.backend.givr.shared.service;

import com.backend.givr.organization.dtos.ApplicationStats;
import com.backend.givr.organization.entity.Organization;
import com.backend.givr.shared.entity.ProjectApplication;
import com.backend.givr.organization.repo.ProjectApplicationRepo;
import com.backend.givr.organization.security.OrganizationDetailsService;
import com.backend.givr.redis.RedisService;
import com.backend.givr.shared.dtos.ProjectApplicationForm;
import com.backend.givr.shared.dtos.VolunteerApplicationDto;
import com.backend.givr.shared.email.EmailService;
import com.backend.givr.shared.entity.Skill;
import com.backend.givr.shared.entity.projects.Project;
import com.backend.givr.shared.enums.ApplicationStatus;
import com.backend.givr.shared.exceptions.DuplicateAccountException;
import com.backend.givr.shared.exceptions.IllegalOperationException;
import com.backend.givr.shared.exceptions.MaxApplicantsReachedException;
import com.backend.givr.shared.exceptions.ProjectDeadlinePastException;
import com.backend.givr.shared.mapper.SkillMapper;
import com.backend.givr.volunteer.entity.Volunteer;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@Slf4j
public class ApplicationService {
    @Autowired
    private ProjectApplicationRepo repo;
    @PersistenceContext
    private EntityManager em;
    @Autowired
    private EmailService emailService;
    @Autowired
    private OrganizationDetailsService organizationDetailsService;
    @Autowired
    private SkillMapper skillMapper;
    @Autowired
    private SkillService skillService;
    @Autowired
    private ParticipationService participationService;
    @Autowired
    private RedisService redisService;
    @Autowired
    private ObjectMapper objectMapper;

    public ProjectApplication apply(Volunteer volunteer, ProjectApplicationForm applicationForm, String email){
        Project project = em.getReference(Project.class, applicationForm.getProjectId());
        String organizerName = project.getOrganizerName();

        if(LocalDateTime.now().isAfter(project.getDeadline().atTime(23, 59, 59)))
            throw new ProjectDeadlinePastException("Cannot applyForProject for a project past it's application period");

        var application = new ProjectApplication(project, volunteer, email);
        application.setApplicationReason(applicationForm.getReason());
        application.setIsAvailable(applicationForm.getIsAvailable());
        application.setAboutVolunteer(applicationForm.getAboutMe());

        if(Objects.nonNull(applicationForm.getAdditionalInfo()))
            application.setAdditionalInfo(applicationForm.getAdditionalInfo());

        if(Objects.nonNull(applicationForm.getMySkills())) {
            Set<Skill> specialSkillSet = skillService.updateSkills(applicationForm.getMySkills());
            application.setSpecialSkills(specialSkillSet.stream().toList());
        }
        try{
            var projectApplication =  repo.save(application);
            emailService.sendApplicationSubmittedEmail(volunteer.getFirstname(), project.getTitle(), organizerName,
                    String.format("%S, %S", project.getAddress(), project.getLocation().getState()), email);

            emailService.sendApplicationNotificationEmail(organizerName, project.getTitle(), email);
            return projectApplication;
        }catch (DataIntegrityViolationException e){
            log.info(e.getLocalizedMessage());
            throw new DuplicateAccountException("Cannot applyForProject to a project more than once");
        }
    }

    public void cancelApplication(Long projectId, Volunteer volunteer){
        Project project = em.getReference(Project.class, projectId);
        repo.deleteByProjectAndVolunteer(project, volunteer);
    }

    public void notifyApplicationChange(ProjectApplication application, Project project, ApplicationStatus status){

        switch (status){
            case APPROVED -> {
                String name = project.getOrganizerName();
                String address = String.format("%s, %S", project.getAddress(), project.getLocation().getState());
                emailService.sendApplicationApproved(application.getVolunteer().getFirstname(), project.getTitle(),
                        name,address, application.getEmail());
            }
            case REJECTED -> {
                String name = project.getOrganizerName();
                emailService.sendApplicationRejected(application.getVolunteer().getFirstname(), project.getTitle(),
                        name, application.getEmail());
            }
        }
    }

    @Transactional
    public void changeApplicationStatus(Long applicationId,ApplicationStatus status){
        if(applicationId==null)
            throw new IllegalArgumentException("Null values are not accepted");

        ProjectApplication application = repo.findById(applicationId).orElseThrow();

        changeApplicationStatus(application, status);
    }


    protected void changeApplicationStatus(ProjectApplication application, ApplicationStatus status){
        Project project = application.getProject();

        if(project.getApprovedList().size() >= project.getMaxVolunteers())
            throw new MaxApplicantsReachedException("Maximum applicants reached");

        if(status == ApplicationStatus.APPLIED)
            throw new IllegalOperationException("Cannot change status to applied");

        if(status == ApplicationStatus.APPROVED)
            participationService.createParticipation(project, application);

        application.setStatus(status);
        repo.save(application);

        if(status == ApplicationStatus.APPROVED){
            redisService.addAuthorizedUserProjects(application.getVolunteer().getVolunteerId(), project.getProjectId());
        }
        notifyApplicationChange(application, project, status);
    }
    private void checkNull(Project project, Volunteer volunteer){
        if(Objects.isNull(volunteer) && Objects.isNull(project))
            throw new IllegalArgumentException("Null values are not accepted");
    }

    public List<ProjectApplication> getAppliedProjects(Volunteer volunteer){
        return repo.findAllByVolunteer(volunteer);
    }

    public List<VolunteerApplicationDto> getProjectsApplications(Organization organization){
        return repo.findAllByOrganizationAndStatus(organization, ApplicationStatus.APPLIED).stream().map(VolunteerApplicationDto::new).toList();
    }

    public PagedModel<VolunteerApplicationDto> getInitiativeApplications(Volunteer individual, PageRequest pageRequest){
        Page<ProjectApplication> page = repo.findAllByCreatorIndividualAndStatus(individual, ApplicationStatus.APPLIED, pageRequest);
        pageRequest = pageRequest.withSort(Sort.by("appliedAt").descending());

        PageImpl<VolunteerApplicationDto> volunteerApplicationDtos = new PageImpl<>(page.getContent().stream().map(VolunteerApplicationDto::new).toList(),
                page.getPageable(), page.getTotalElements());

        return new PagedModel<>(volunteerApplicationDtos);
    }

    public ApplicationStats getVolunteerStats(Organization organization){
        int approved = repo.countByStatusAndOrganization(ApplicationStatus.APPLIED, organization);
        int applied = repo.countByStatusAndOrganization(ApplicationStatus.APPLIED, organization);
        int rejected = repo.countByStatusAndOrganization(ApplicationStatus.REJECTED, organization);

        return new ApplicationStats(applied, approved, rejected);
    }
}
