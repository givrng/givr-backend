package com.backend.givr.volunteer.service;

import com.backend.givr.organization.dtos.UpdateParticipantDto;
import com.backend.givr.organization.security.ProjectServiceWorker;
import com.backend.givr.redis.RedisService;
import com.backend.givr.shared.dtos.*;
import com.backend.givr.shared.entity.Creator;
import com.backend.givr.shared.entity.projects.Donation;
import com.backend.givr.shared.entity.projects.Initiative;
import com.backend.givr.shared.enums.*;
import com.backend.givr.shared.exceptions.IllegalOperationException;
import com.backend.givr.shared.interfaces.SecurityDetails;
import com.backend.givr.shared.mapper.ProjectMapper;
import com.backend.givr.shared.repo.DonationRepo;
import com.backend.givr.shared.service.*;
import com.backend.givr.volunteer.dtos.FreeInitiativeCount;
import com.backend.givr.volunteer.entity.Volunteer;
import com.backend.givr.volunteer.repo.VolunteerRepo;
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
    private final VolunteerRepo volunteerRepo;
    private final CreatorService creatorService;
    private final InitiativeApplicationService initiativeApplicationService;

    public Volunteer getIndividual(String volunteerId){
        return volunteerRepo.findById(volunteerId).orElseThrow(()->new EntityNotFoundException("Account does not exist"));
    }

    public InitiativeResponseDto createInitiative(InitiativeRequestDto payload, SecurityDetails authContext) {
        if(payload == null)
            throw new IllegalArgumentException("Null project DTO null accepted");

        Volunteer individual = getIndividual(authContext.getId());
        if(individual.getVerificationStatus() != VerificationStatus.VERIFIED)
            throw new IllegalOperationException("Volunteer account must be verified to create an initiative");

        var initiative = initiativeService.createInitiative(payload, individual);
        return projectMapper.toInitiative(initiative);
    }
    
    public InitiativeResponseDto updateProject(Long projectId, InitiativeRequestDto projectRequestDto, String individualId) {
        Volunteer individual = getIndividual(individualId);
        return projectMapper.toInitiative(initiativeService.updateInitiative(projectId, individual, projectRequestDto));
    }

    public PagedModel<VolunteerApplicationDto> getProjectApplications(SecurityDetails details, PageRequest pageRequest) {
        return initiativeApplicationService.getInitiativeApplications(details.getId(), pageRequest);
    }


    public void approveApplication(Long applicationId) {
        initiativeApplicationService.updateApplicationStatus(applicationId, ApplicationStatus.APPROVED);
    }

    public void rejectApplication(Long applicationId) {
        initiativeApplicationService.updateApplicationStatus(applicationId, ApplicationStatus.REJECTED);
    }

    public void deleteInitiative(Long projectId, String volunteerId) {
        Volunteer individual = getIndividual(volunteerId);
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
        Volunteer individual = getIndividual(id);
        participationService.markInitiativeCompleted(projectId, individual);
    }


    public @Nullable PagedModel<InitiativeResponseDto> getInitiatives(ProjectStatus status, int pageNum, int pageSize, String volunteerId) {
        Volunteer volunteer = getIndividual(volunteerId);
        PageRequest pageRequest = PageRequest.of(pageNum, pageSize).withSort(Sort.by("createdAt").descending());

        Page<Initiative> initiatives = initiativeService.getAllInitiativeByIndividualAndStatus(volunteer, status, pageRequest);

        System.out.println(initiatives.getContent().size());
        PageImpl<InitiativeResponseDto> page = new PageImpl<>(projectMapper.toInitiativeReponses(initiatives.getContent()),
                initiatives.getPageable(), initiatives.getTotalElements());

        return new PagedModel<>(page);
    }

    public DonationDto getDonation(String donationId) {

        return projectMapper.toDonationDto(donationRepo.findById(donationId)
                .orElseThrow(()->new EntityNotFoundException("Donation does not exist")));
    }

    public PagedModel<DonationDto> getPersonalDonations(String id, DonationStatus status, PageRequest pageRequest) {
        Volunteer creator = getIndividual(id);
        pageRequest = pageRequest.withSort(Sort.by("createdAt").descending());

        Page<Donation> donationsPage = status == null? donationRepo.findAllByDonor(creator, pageRequest):
                donationRepo.findAllByDonorAndStatus(creator, status, pageRequest);

        PageImpl<DonationDto> donationDtoPage = new PageImpl<>(projectMapper.toDonationDtos(donationsPage.getContent()),
                donationsPage.getPageable(), donationsPage.getTotalElements());

        return new PagedModel<>(donationDtoPage);
    }

    public PagedModel<DonationDto> getDonations(String id, DonationStatus status, PageRequest pageRequest) {
        Creator creator = creatorService.getCreatorByIndividualId(id);
        pageRequest = pageRequest.withSort(Sort.by("createdAt").descending());

        Page<Donation> donationsPage = status == null? donationRepo.findAllByItemInitiativeCreator(creator, pageRequest):
                donationRepo.findAllByItemInitiativeCreatorAndStatus(creator, status, pageRequest);

        List<DonationDto> donationDtos = projectMapper.toDonationDtos(donationsPage.getContent());

        PageImpl<DonationDto> donationDtoPage = new PageImpl<>(donationDtos, donationsPage.getPageable(), donationsPage.getTotalElements());

        return new PagedModel<>(donationDtoPage);
    }



    public @Nullable DonationDto updateDonationStatus(@Valid DonationUpdateDto donationUpdateDto, String individualId) {
       return projectMapper.toDonationDto(initiativeApplicationService.updateUpdateDonation(donationUpdateDto, individualId));
    }

    public @Nullable List<InitiativeItemDto> getInitiativeDonations(Long initiativeId, SecurityDetails details) {
        Creator creator = creatorService.getCreatorByIndividualId(details.getId());
        Initiative initiative = initiativeService.getInitiativeByIdAndCreator(initiativeId, creator);

        List<InitiativeItemDto> resources = projectMapper.toInitiativeItemDtos(initiative.getInitiativeItems());
        resources.addAll(projectMapper.toInitiativeItemDtos(initiative.getInitiativeServices()));

        return resources;
    }

    public FreeInitiativeCount getFreeInitiativeCreationCount(SecurityDetails details) {
        return new FreeInitiativeCount(creatorService.getCreatorByIndividualId(details.getId()).getFreeProjectCount());
    }
}
