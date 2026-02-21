package com.backend.givr.organization.service;

import com.backend.givr.organization.entity.Organization;
import com.backend.givr.organization.entity.Participation;
import com.backend.givr.organization.entity.Project;
import com.backend.givr.organization.entity.ProjectApplication;
import com.backend.givr.organization.repo.ParticipationRepo;
import com.backend.givr.shared.enums.ApplicationStatus;
import com.backend.givr.shared.enums.ParticipationStatus;
import com.backend.givr.shared.exceptions.IllegalOperationException;
import com.backend.givr.shared.email.EmailService;
import com.backend.givr.volunteer.entity.Volunteer;
import com.backend.givr.volunteer.security.VolunteerDetailsService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ParticipationService {

    @Autowired
    private ParticipationRepo repo;
    @Autowired
    private VolunteerDetailsService detailsService;
    @Autowired
    private EmailService emailService;

    public void createParticipation(Project project, ProjectApplication application){
        Participation participation = new Participation();
        participation.setVolunteer(application.getVolunteer());
        participation.setProjectApplication(application);
        participation.setProject(project);
        participation.setOrganization(project.getOrganization());
        participation.setParticipationStatus(ParticipationStatus.IN_PROGRESS);
        repo.save(participation);
    }

    public List<Participation> getVolunteerParticipation(Volunteer volunteer){
        return repo.findAllByVolunteer(volunteer);
    }

    public List<Participation> getParticipantsByOrganization(Organization organization){
        return repo.findAllByOrganization(organization);
    }

    @Transactional
    public void changeParticipationStatus( Long participationId, ParticipationStatus status){
        Participation participation = repo.findById(participationId).orElseThrow(()->new EntityNotFoundException(String.format("Participant with participationId %s, not not found", participationId)));
        Project project = participation.getProject();
        Volunteer volunteer = participation.getVolunteer();

        if(status == ParticipationStatus.IN_PROGRESS)
            throw new IllegalOperationException("Illegal operation, cannot change a participation to in-progress");

        if(!participation.getReviewable() && status == ParticipationStatus.COMPLETED)
            return ;

        participation.setParticipationStatus(status);

        // Send notification to volunteer
        emailService.sendParticipationUpdate(volunteer.getFirstname(), project.getTitle(),
                detailsService.getEmail(volunteer), project.getOrganization().getOrganizationName(), status);

        if(status == ParticipationStatus.REJECTED){
            participation.getProjectApplication().setStatus(ApplicationStatus.REJECTED);
        }
    }

    public void deleteVolunteerParticipation(Long participationId, Volunteer volunteer){
        Participation participation = repo.findByIdAndVolunteer(participationId, volunteer).orElseThrow(()->new IllegalOperationException("Illegal operation, cannot perform operation delete"));
        repo.delete(participation);
    }

    public void updateReviewable(){

    }
}
