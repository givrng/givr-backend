package com.backend.givr.organization.service;

import com.backend.givr.organization.dtos.ApplicationStats;
import com.backend.givr.organization.entity.Organization;
import com.backend.givr.organization.entity.Project;
import com.backend.givr.organization.entity.ProjectApplication;
import com.backend.givr.organization.repo.ProjectApplicationRepo;
import com.backend.givr.shared.dtos.ProjectApplicationForm;
import com.backend.givr.shared.dtos.VolunteerApplicationDto;
import com.backend.givr.shared.email.EmailService;
import com.backend.givr.shared.enums.ApplicationStatus;
import com.backend.givr.shared.exceptions.IllegalOperationException;
import com.backend.givr.shared.exceptions.MaxApplicantsReachedException;
import com.backend.givr.shared.exceptions.ProjectDeadlinePastException;
import com.backend.givr.volunteer.entity.Volunteer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class ApplicationService {
    @Autowired
    private ProjectApplicationRepo repo;
    @PersistenceContext
    private EntityManager em;
    @Autowired
    private EmailService emailService;

    @Autowired
    private ParticipationService participationService;

    public ProjectApplication apply(Volunteer volunteer, ProjectApplicationForm applicationForm, String email){
        Project project = em.getReference(Project.class, applicationForm.projectId());
        if(LocalDateTime.now().isAfter(project.getDeadline().atTime(23, 59, 59)))
            throw new ProjectDeadlinePastException("Cannot apply for a project past it's application period");

        var application = new ProjectApplication(project, volunteer, email);
        application.setApplicationReason(application.getApplicationReason());
        application.setAvailableDays(application.getAvailableDays());
        try{
            var projectApplication =  repo.save(application);

            emailService.sendApplicationSubmittedEmail(volunteer.getFirstname(), project.getTitle(), project.getOrganization().getOrganizationName(),
                    String.format("%s,%s", project.getLocation().getLga(), project.getLocation().getState()), email);

            return projectApplication;
        }catch (DataIntegrityViolationException ignored){
            throw new DataIntegrityViolationException("Cannot apply to a project more than once");
        }
    }

    public void cancelApplication(Long projectId, Volunteer volunteer){
        Project project = em.getReference(Project.class, projectId);
        repo.deleteByProjectAndVolunteer(project, volunteer);
    }

    public void notifyApplicationChange(ProjectApplication application, Project project, ApplicationStatus status){

        switch (status){
            case APPROVED -> {
                String address = String.format("%s, %s", project.getLocation().getLga(), project.getLocation().getState());
                emailService.sendApplicationApproved(application.getVolunteer().getFirstname(), project.getTitle(),
                        project.getOrganization().getOrganizationName(),address, application.getEmail());
            }
            case REJECTED -> {
                emailService.sendApplicationRejected(application.getVolunteer().getFirstname(), project.getTitle(),
                        project.getOrganization().getOrganizationName(), application.getEmail());
            }
        }
    }

    @Transactional
    public void changeApplicationStatus(Long applicationId, ApplicationStatus status){
        if(applicationId==null)
            throw new IllegalArgumentException("Null values are not accepted");

        ProjectApplication application = repo.findById(applicationId).orElseThrow();
        Project project = application.getProject();

        if(project.getApprovedList().size() >= project.getMaxVolunteers())
            throw new MaxApplicantsReachedException("Maximum applicants reached");

        if(status == ApplicationStatus.APPLIED)
            throw new IllegalOperationException("Cannot change status to applied");

        if(status == ApplicationStatus.APPROVED)
            participationService.createParticipation(project, application);

        application.setStatus(status);
        repo.save(application);
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

    public ApplicationStats getVolunteerStats(Organization organization){
        int approved = repo.countByStatus(ApplicationStatus.APPLIED);
        int applied = repo.countByStatus(ApplicationStatus.APPLIED);
        int rejected = repo.countByStatus(ApplicationStatus.REJECTED);

        return new ApplicationStats(applied, approved, rejected);
    }
}
