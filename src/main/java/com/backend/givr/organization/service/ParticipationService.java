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
import com.resend.core.exception.ResendException;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class ParticipationService {

    @Autowired
    private ParticipationRepo repo;
    @Autowired
    private VolunteerDetailsService detailsService;
    @Autowired
    private EmailService emailService;

    private final Logger logger = LoggerFactory.getLogger(ParticipationService.class);

    public void createParticipation(Project project, ProjectApplication application){
        Participation participation = new Participation();
        participation.setVolunteer(application.getVolunteer());
        participation.setProjectApplication(application);
        participation.setProject(project);
        participation.setOrganization(project.getOrganization());
        participation.setParticipationStatus(ParticipationStatus.IN_PROGRESS);

        createContact(repo.save(participation), project.getSegmentId());
    }

    @Async
    private void createContact(Participation participation, String segmentId){
        Volunteer volunteer = participation.getVolunteer();
        try{
            String contactId = emailService.createContact(volunteer.getEmail(), volunteer.getFirstname(), volunteer.getLastname());
            emailService.addContactToSegment(segmentId, contactId);
            participation.setContactId(contactId);
            participation.setIsUnSubscribed(false);
            repo.save(participation);
        } catch (ResendException e) {
            logger.error("Failed to create contact, {}", e.getLocalizedMessage());
        }
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
            participation.setIsUnSubscribed(true);
            participation.setContactId(null);
            try{
                emailService.removeParticipantFromSegment(participation.getVolunteer().getEmail(), project.getSegmentId());
            } catch (ResendException e) {
                logger.error("Failed to removed contact from segment, {}", e.getLocalizedMessage());
            }
        }
    }

    public void deleteVolunteerParticipation(Long participationId, Volunteer volunteer){
        Participation participation = repo.findByIdAndVolunteer(participationId, volunteer).orElseThrow(()->new IllegalOperationException("Illegal operation, cannot perform operation delete"));
        repo.delete(participation);
    }

    public void updateReviewable(){

    }
}
