package com.backend.givr.shared.service;

import com.backend.givr.organization.entity.Organization;
import com.backend.givr.shared.entity.Participation;
import com.backend.givr.shared.entity.projects.Initiative;
import com.backend.givr.shared.entity.ProjectApplication;
import com.backend.givr.organization.repo.ParticipationRepo;
import com.backend.givr.organization.repo.ProjectApplicationRepo;
import com.backend.givr.redis.RedisService;
import com.backend.givr.shared.dtos.ParticipationDto;
import com.backend.givr.shared.dtos.RatingDTO;
import com.backend.givr.shared.entity.projects.Project;
import com.backend.givr.shared.enums.ApplicationStatus;
import com.backend.givr.shared.enums.CertificationStatus;
import com.backend.givr.shared.enums.ParticipationStatus;
import com.backend.givr.shared.enums.ProjectStatus;
import com.backend.givr.shared.exceptions.IllegalOperationException;
import com.backend.givr.shared.email.EmailService;
import com.backend.givr.shared.mapper.ProjectMapper;
import com.backend.givr.volunteer.entity.Volunteer;
import com.backend.givr.volunteer.security.VolunteerDetailsService;
import com.resend.core.exception.ResendException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.*;
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
    private CreatorService creatorService;
    @Autowired
    private InitiativeService initiativeService;
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

        redisService.addProjectParticipantEmail(project.getProjectId(), List.of(email));
        participation.setVolunteer(volunteer);
        participation.setProjectApplication(application);
        participation.setProject(project);
        if(project.getCreator() != null){
            participation.setCreator(project.getCreator());
        }
        participation.setParticipationStatus(ParticipationStatus.IN_PROGRESS);

        try{
            String contactId = emailService.createContact(email, volunteer.getFirstname(), volunteer.getLastname());
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


    public PagedModel<ParticipationDto> getParticipantsByOrgOrCreator(Organization organization, PageRequest pageRequest){
        // Try retrieving using old and new field
        Page<Participation> participationPage = repo.getAllByCreatorOrOrganization(organization, pageRequest);
        // Create a creator for older records
        // Save to database
        // Convert to DTO
        // Never mind, this will be done while fetching projects
        List<ParticipationDto> dtos = mapper.toParticipationDto(participationPage.getContent());
        return new PagedModel<>(new PageImpl<>(dtos, participationPage.getPageable(), participationPage.getTotalElements()));
    }

    @Transactional
    private void updateParticipationStatus(Participation participation, ParticipationStatus status){
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

    public void changeIndParticipationStatus(Long participationId, Volunteer individual, ParticipationStatus status){
        Participation participation = repo.findByIdAndCreatorIndividual(participationId, individual)
                .orElseThrow(()->new EntityNotFoundException(String.format("Participant with participationId %s, not not found", participationId)));
        updateParticipationStatus(participation, status);
    }

    public void changeOrgParticipationStatus(Long participationId, Organization organization, ParticipationStatus status){
        Participation participation = repo.findByIdAndOrganizationOrCreatorOrganization(participationId, organization, organization)
                .orElseThrow(()->new EntityNotFoundException(String.format("Participant with participationId %s, not not found", participationId)));
        updateParticipationStatus(participation, status);
    }

    @Async
    @Transactional
    public void markInitiativeCompleted(Long initiativeId, Volunteer individual){
        Initiative initiative = initiativeService.getInitiative(initiativeId, individual);
        markProjectCompleted(initiative);
    }

    @Async
    @Transactional
    public void markProjectCompleted(Long projectId, Organization organization){
        Project project = projectService.getProject(projectId);

        if(!project.getOrganization().equals(organization) && !project.getCreator().getOrganization().equals(organization))
            throw new EntityNotFoundException("BaseProject not found for organization "+ organization.getOrganizationId());

        markProjectCompleted(project);
    }

    public void markProjectCompleted(Project project) {
        project.setStatus(ProjectStatus.COMPLETED);
        Flux<Participation> participationList = Flux.fromIterable(getParticipationByProject(project));

        participationList.parallel().doOnNext(p->{
                    p.setParticipationStatus(ParticipationStatus.COMPLETED);
                    p.setCertificationStatus(CertificationStatus.Pending);
                    repo.save(p);
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
        return repo.findAllByProjectProjectId(project.getProjectId());
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
        Page<Participation> participation = repo.findAllByCertificationStatusAndParticipationStatus(CertificationStatus.Pending,ParticipationStatus.COMPLETED, pageable);
        if(participation.isEmpty())
            participation = repo.findAllByCertificationStatusAndParticipationStatus(null, ParticipationStatus.COMPLETED, pageable);

        return new PagedModel<>(participation.map(mapper::toParticipationDto));
    }

    public @Nullable List<ParticipationDto> getParticipantsByIndividualId(String individualId) {
        Volunteer individual = em.getReference(Volunteer.class, individualId);
        return mapper.toParticipationDto(repo.findAllByProjectCreatorIndividual(individual));
    }
}
