package com.backend.givr.organization.service;

import com.backend.givr.organization.entity.Organization;
import com.backend.givr.organization.entity.Participation;
import com.backend.givr.organization.entity.Project;
import com.backend.givr.organization.entity.ProjectApplication;
import com.backend.givr.organization.repo.ParticipationRepo;
import com.backend.givr.organization.repo.ProjectApplicationRepo;
import com.backend.givr.redis.RedisService;
import com.backend.givr.shared.dtos.ParticipationDto;
import com.backend.givr.shared.dtos.RatingDTO;
import com.backend.givr.shared.enums.ApplicationStatus;
import com.backend.givr.shared.enums.CertificationStatus;
import com.backend.givr.shared.enums.ParticipationStatus;
import com.backend.givr.shared.enums.ProjectStatus;
import com.backend.givr.shared.exceptions.IllegalOperationException;
import com.backend.givr.shared.email.EmailService;
import com.backend.givr.shared.mapper.ProjectMapper;
import com.backend.givr.shared.service.RatingService;
import com.backend.givr.volunteer.entity.Volunteer;
import com.backend.givr.volunteer.security.VolunteerDetailsService;
import com.resend.core.exception.ResendException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.web.PagedModel;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

import java.time.Duration;
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
    private ProjectService projectService;
    @Autowired
    private VolunteerDetailsService detailsService;
    @Autowired
    private EmailService emailService;
    @Autowired
    private RatingService ratingService;
    @Autowired
    private RedisService redisService;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private ProjectMapper mapper;
    @Autowired
    private EntityManager em;

    @Async
    @CacheEvict(
            cacheNames = "participantEmails",
            key = "#project.projectId"
    )
    public void createParticipation(Project project, ProjectApplication application){
        Participation participation = new Participation();
        Volunteer volunteer = application.getVolunteer();
        String email = volunteer.getEmail();
        String segmentId = project.getSegmentId();

        redisService.addProjectParticipantEmail(project.getProjectId(), List.of(email));
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
            log.error("Failed to create contact, {}", e.getLocalizedMessage());
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

        if(status == ParticipationStatus.COMPLETED) {
            participation.setCertificationStatus(CertificationStatus.Pending);
            participation.setParticipationStatus(ParticipationStatus.COMPLETED);
        }
        participation.setParticipationStatus(status);

        // Send notification to volunteer
        emailService.sendParticipationUpdate(volunteer, project, status);

        if(status == ParticipationStatus.REJECTED){
            repo.delete(participation);
            redisService.removeAuthorizedUserProject(volunteer.getVolunteerId(), project.getProjectId());
            emailService.sendParticipationUpdate(volunteer, project, ParticipationStatus.REJECTED);
        }
    }

    @Async
    @Transactional
    public void markProjectCompleted(Long projectId) {
        Project project = projectService.findProjectById(projectId);
        project.setStatus(ProjectStatus.COMPLETED);
        Flux<Participation> participationList = Flux.fromIterable(getParticipationByProject(project));

        participationList.parallel().doOnNext(p->{
                    p.setParticipationStatus(ParticipationStatus.COMPLETED);
                    p.setCertificationStatus(CertificationStatus.Pending);
                })
                .sequential()
                .delayElements(Duration.ofMillis(200))
                .doOnNext(p->{
                    emailService.sendParticipationUpdate(p.getVolunteer(), project, ParticipationStatus.COMPLETED);
                }).doOnError(err->{
                    log.error("An error occurred while updating participation status {}", err.getLocalizedMessage());
                })
                .doOnComplete(()->log.info("Participant has been notified"))
                .subscribe();

    }

    public void deleteVolunteerParticipation(Long participationId, Volunteer volunteer){
        Participation participation = repo.findByIdAndVolunteer(participationId, volunteer).orElseThrow(()->new IllegalOperationException("Illegal operation, cannot perform operation delete"));
        repo.delete(participation);
    }

    public List<Participation> getParticipationByProject( Project project){
        return repo.findAllByProject( project);
    }

    @Cacheable(
            cacheNames = "participantEmails",
            key = "#projectId"
    )
    public List<String> getVolunteerParticipationEmail(Long projectId){
        Project project = em.getReference(Project.class, projectId);
        return getParticipationByProject(project).stream().map(Participation::getVolunteer).map(Volunteer::getEmail).toList();
    }

    @Async
    public void createRating(Long participationId, Volunteer volunteer, RatingDTO ratingDTO) {
        Optional<Participation> participation = repo.findByIdAndVolunteer(participationId, volunteer);
        participation.ifPresent(part-> ratingService.addOrUpdateRating(volunteer, part.getProject().getProjectId(), ratingDTO.rating()));
    }

    public PagedModel<ParticipationDto> findParticipantsPendingCertification(int pageNum, int pageSize){
        Pageable pageable = PageRequest.of(pageNum, pageSize, Sort.by("updatedAt").descending());
        Page<Participation> participation = repo.findAllByCertificationStatus(CertificationStatus.Pending, pageable);
        if(participation.isEmpty())
            participation = repo.findAllByCertificationStatus(null, pageable);

        return new PagedModel<>(participation.map(mapper::toParticipationDto));
    }
}
