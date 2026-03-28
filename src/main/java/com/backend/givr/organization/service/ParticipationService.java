package com.backend.givr.organization.service;

import com.backend.givr.organization.entity.Organization;
import com.backend.givr.organization.entity.Participation;
import com.backend.givr.organization.entity.Project;
import com.backend.givr.organization.entity.ProjectApplication;
import com.backend.givr.organization.repo.ParticipationRepo;
import com.backend.givr.organization.repo.ProjectApplicationRepo;
import com.backend.givr.shared.dtos.RatingDTO;
import com.backend.givr.shared.enums.ApplicationStatus;
import com.backend.givr.shared.enums.ParticipationStatus;
import com.backend.givr.shared.exceptions.IllegalOperationException;
import com.backend.givr.shared.email.EmailService;
import com.backend.givr.shared.service.RatingService;
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
import java.util.Optional;

@Service
@Slf4j
public class ParticipationService {

    @Autowired
    private ParticipationRepo repo;
    @Autowired
    private ProjectApplicationRepo applicationRepo;
    @Autowired
    private VolunteerDetailsService detailsService;
    @Autowired
    private EmailService emailService;
    @Autowired
    private RatingService ratingService;

    private final Logger logger = LoggerFactory.getLogger(ParticipationService.class);

    @Async
    public void createParticipation(Project project, ProjectApplication application){
        Participation participation = new Participation();
        Volunteer volunteer = application.getVolunteer();
        String email = volunteer.getEmail();
        String segmentId = project.getSegmentId();

        participation.setVolunteer(volunteer);
        participation.setProjectApplication(application);
        participation.setProject(project);
        participation.setOrganization(project.getOrganization());
        participation.setParticipationStatus(ParticipationStatus.IN_PROGRESS);

        try{
            String contactId = emailService.createContact(email, volunteer.getFirstname(), volunteer.getLastname());
            emailService.addContactToSegment(segmentId, contactId);
            participation.setContactId(contactId);
            participation.setIsUnSubscribed(false);
            repo.save(participation);
        } catch (ResendException e) {
            logger.error("Failed to create contact, {}", e.getLocalizedMessage());
            System.err.printf("Failed to create participant because contact was not created. Contact was not created because %S", e.getLocalizedMessage());
            application.setStatus(ApplicationStatus.APPLIED);
            applicationRepo.save(application);
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

        if(participation.getParticipationStatus() == status)
            return;

        participation.setParticipationStatus(status);

        // Send notification to volunteer
        emailService.sendParticipationUpdate(volunteer, project, status);

        if(status == ParticipationStatus.REJECTED){
            repo.delete(participation);
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

    public Participation getParticipationByVolunteerAndProject(Volunteer volunteer, Project project){
        return repo.findByVolunteerAndProject(volunteer, project).orElseThrow(()->new EntityNotFoundException(String.format("Volunteer %s is not a participant of %s project", volunteer.getVolunteerId(), project.getProjectId())));
    }

    @Async
    public void createRating(Long participationId, Volunteer volunteer, RatingDTO ratingDTO) {
        Optional<Participation> participation = repo.findByIdAndVolunteer(participationId, volunteer);
        participation.ifPresent(part-> ratingService.addOrUpdateRating(volunteer, part.getProject().getProjectId(), ratingDTO.rating()));
    }
}
