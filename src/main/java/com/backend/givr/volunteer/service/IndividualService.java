package com.backend.givr.volunteer.service;

import com.backend.givr.organization.dtos.UpdateParticipantDto;
import com.backend.givr.organization.security.ProjectServiceWorker;
import com.backend.givr.redis.RedisService;
import com.backend.givr.shared.dtos.*;
import com.backend.givr.shared.entity.Creator;
import com.backend.givr.shared.entity.projects.Donation;
import com.backend.givr.shared.entity.projects.Initiative;
import com.backend.givr.shared.enums.ApplicationStatus;
import com.backend.givr.shared.enums.DonationStatus;
import com.backend.givr.shared.enums.ProjectStatus;
import com.backend.givr.shared.enums.VerificationStatus;
import com.backend.givr.shared.exceptions.IllegalOperationException;
import com.backend.givr.shared.interfaces.SecurityDetails;
import com.backend.givr.shared.mapper.ProjectMapper;
import com.backend.givr.shared.repo.DonationRepo;
import com.backend.givr.shared.service.*;
import com.backend.givr.volunteer.entity.Volunteer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedModel;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IndividualService {
    private final ProjectService projectService;
    private final InitiativeService initiativeService;
    private final ProjectMapper projectMapper;
    private final RedisService redisService;
    private final ProjectServiceWorker projectServiceWorker;
    private final ParticipationService participationService;
    private final EntityManager em;
    private final DonationRepo donationRepo;
    private final VolunteerService volunteerService;
    private final CreatorService creatorService;
    private final InitiativeApplicationService initiativeApplicationService;

    public Volunteer getIndividual(String volunteerId){
        return volunteerService.getIndividual(volunteerId);
    }

    public List<InitiativeResponseDto> createInitiative(InitiativeRequestDto payload, SecurityDetails authContext) {
        if(payload == null)
            throw new IllegalArgumentException("Null project DTO null accepted");

        Volunteer individual = getIndividual(authContext.getId());
        if(individual.getVerificationStatus() != VerificationStatus.VERIFIED)
            throw new IllegalOperationException("Volunteer account must be verified to create an initiative");

        var initiative = initiativeService.createInitiative(payload, individual);
        return projectMapper.toInitiativeReponses(initiativeService.getAllInitiativeByIndividualAndStatus(initiative.getCreator(), ProjectStatus.DRAFT));
    }
    
    public InitiativeResponseDto updateProject(Long projectId, InitiativeRequestDto projectRequestDto, String individualId) {
        Volunteer individual = volunteerService.getIndividual(individualId);
        return projectMapper.toInitiative(initiativeService.updateInitiative(projectId, individual, projectRequestDto));
    }

    public PagedModel<VolunteerApplicationDto> getProjectApplications(SecurityDetails details, PageRequest pageRequest) {
        Volunteer individual = getIndividual(details.getId());
        return initiativeApplicationService.getInitiativeApplications(creatorService.createOrReturnCreator(individual), pageRequest);
    }


    public void approveApplication(String applicationId) {
        initiativeApplicationService.updateApplicationStatus(applicationId, ApplicationStatus.APPROVED);
    }

    public void rejectApplication(String applicationId) {
        initiativeApplicationService.updateApplicationStatus(applicationId, ApplicationStatus.REJECTED);
    }

    public void publishProject(Long projectId, SecurityDetails details) {
        Initiative project = initiativeService.findInitiativeById(projectId);

        // Grants individual access to project in-app messages
        redisService.addAuthorizedUserProjects(details.getId(), projectId);
        project.setStatus(ProjectStatus.OPEN);

        try{
            initiativeService.save(project);
            projectServiceWorker.sendProjectListing(project).subscribe();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteInitiative(Long projectId, String volunteerId) {
        Volunteer individual = em.getReference(Volunteer.class, volunteerId);
        initiativeService.deleteInitiative(projectId, individual);
    }

    public void updateVolunteerParticipation(UpdateParticipantDto payload, String individualId) {
        Volunteer individual = em.getReference(Volunteer.class, individualId);
        participationService.changeIndParticipationStatus(payload.id(), individual, payload.status());
    }

    public @Nullable List<ParticipationDto> getProjectParticipants(SecurityDetails details) {
        return participationService.getParticipantsByIndividualId(details.getId());
    }

    public void markInitiativeComplete(Long projectId, String id) {
        Volunteer individual = volunteerService.getIndividual(id);
        participationService.markInitiativeCompleted(projectId, individual);
    }

    public @Nullable PagedModel<InitiativeResponseDto> getInitiatives(ProjectStatus status, int pageNum, int pageSize, String volunteerId) {
        Volunteer volunteer = getIndividual(volunteerId);
        PageRequest pageRequest = PageRequest.of(pageNum, pageSize).withSort(Sort.by("createdAt").descending());

        Page<Initiative> initiatives = initiativeService.getAllInitiativeByIndividualAndStatus(volunteer, status, pageRequest);

        PageImpl<InitiativeResponseDto> page = new PageImpl<>(projectMapper.toInitiativeReponses(initiatives.getContent()),
                initiatives.getPageable(), initiatives.getTotalElements());

        return new PagedModel<>(page);
    }

    public DonationDto getDonation(String donationId) {

        return projectMapper.toDonationDto(donationRepo.findById(donationId)
                .orElseThrow(()->new EntityNotFoundException("Donation does not exist")));
    }

    public PagedModel<DonationDto> getDonations(String id, PageRequest pageRequest) {
        Creator creator = creatorService.getCreatorByIndividualId(id);
        pageRequest = pageRequest.withSort(Sort.by("createdAt").descending());
        Page<Donation> donationsPage = donationRepo.findAllByItemInitiativeCreator(creator, pageRequest);
        PageImpl<DonationDto> donationDtoPage = new PageImpl<>(projectMapper.toDonationDtos(donationsPage.getContent()),
                donationsPage.getPageable(), donationsPage.getTotalElements());

        return new PagedModel<>(donationDtoPage);
    }

    public @Nullable DonationDto updateDonationStatus(@Valid DonationUpdateDto donationUpdateDto, String individualId) {
       return projectMapper.toDonationDto(initiativeApplicationService.updateUpdateDonation(donationUpdateDto, individualId));
    }
}
