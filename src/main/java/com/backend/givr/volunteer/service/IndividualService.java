package com.backend.givr.volunteer.service;

import com.backend.givr.organization.dtos.ProjectRequestDto;
import com.backend.givr.organization.dtos.UpdateParticipantDto;
import com.backend.givr.organization.security.ProjectServiceWorker;
import com.backend.givr.redis.RedisService;
import com.backend.givr.shared.dtos.InitiativeResponseDto;
import com.backend.givr.shared.dtos.ParticipationDto;
import com.backend.givr.shared.dtos.VolunteerApplicationDto;
import com.backend.givr.shared.entity.Project;
import com.backend.givr.shared.enums.ApplicationStatus;
import com.backend.givr.shared.enums.ProjectStatus;
import com.backend.givr.shared.enums.VerificationStatus;
import com.backend.givr.shared.exceptions.IllegalOperationException;
import com.backend.givr.shared.interfaces.SecurityDetails;
import com.backend.givr.shared.mapper.ProjectMapper;
import com.backend.givr.shared.service.ApplicationService;
import com.backend.givr.shared.service.ParticipationService;
import com.backend.givr.shared.service.ProjectService;
import com.backend.givr.volunteer.entity.Individual;
import com.backend.givr.volunteer.repo.IndividualRepo;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IndividualService {
    private final IndividualRepo repo;
    private final ProjectService projectService;
    private final ProjectMapper projectMapper;
    private final RedisService redisService;
    private final ProjectServiceWorker projectServiceWorker;
    private final ApplicationService applicationService;
    private final ParticipationService participationService;
    private final EntityManager em;

    public Individual getIndividual(String volunteerId){
        return repo.findById(volunteerId).orElseThrow(()->new EntityNotFoundException("Volunteer does not have an individual account"));
    }

    public List<InitiativeResponseDto> createInitiative(ProjectRequestDto payload, SecurityDetails authContext) {
        if(payload == null)
            throw new IllegalArgumentException("Null project DTO null accepted");

        Individual individual = getIndividual(authContext.getId());
        if(individual.getStatus() != VerificationStatus.VERIFIED)
            throw new IllegalOperationException("Volunteer account must be verified to create an initiative");

        projectService.createProject(payload, individual);
        return projectMapper.toInitiativeReponses(projectService.getProjectByIndividualAndStatus(individual, ProjectStatus.DRAFT));
    }

    public InitiativeResponseDto updateProject(Long projectId, ProjectRequestDto projectRequestDto, String individualId) {
        Individual individual = em.getReference(Individual.class, individualId);
        return projectMapper.toInitiative(projectService.indUpdateProject(projectId, individual, projectRequestDto));
    }

    public List<VolunteerApplicationDto> getProjectApplications(SecurityDetails details) {
        Individual individual = getIndividual(details.getId());
        return applicationService.getInitiativeApplications(individual);
    }

    public void approveApplication(Long applicationId) {
        applicationService.changeApplicationStatus(applicationId, ApplicationStatus.APPROVED);
    }

    public void rejectApplication(Long applicationId) {
        applicationService.changeApplicationStatus(applicationId, ApplicationStatus.REJECTED);
    }

    public void publishProject(Long projectId, SecurityDetails details) {
        Project project = projectService.findProjectById(projectId);
        // Grants organization access to project in-app messages
        redisService.addAuthorizedUserProjects(details.getId(), projectId);
        project.setStatus(ProjectStatus.OPEN);
        try{
            projectService.save(project);
            projectServiceWorker.sendProjectListing(project).subscribe();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteProject(Long projectId, String volunteerId) {
        Individual individual = em.getReference(Individual.class, volunteerId);
        projectService.deleteProject(projectId, individual);
    }

    public void updateVolunteerParticipation(UpdateParticipantDto payload, String individualId) {
        Individual individual = em.getReference(Individual.class, individualId);
        participationService.changeIndParticipationStatus(payload.id(), individual, payload.status());
    }

    public @Nullable List<ParticipationDto> getProjectParticipants(SecurityDetails details) {
        return participationService.getParticipantsByIndividualId(details.getId());
    }
}
