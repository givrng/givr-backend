package com.backend.givr.shared.service;

import com.backend.givr.shared.dtos.DonationUpdateDto;
import com.backend.givr.shared.dtos.InitiativeApplicationForm;
import com.backend.givr.shared.dtos.VolunteerApplicationDto;
import com.backend.givr.shared.entity.Creator;
import com.backend.givr.shared.entity.InitiativeApplication;
import com.backend.givr.shared.entity.projects.Donation;
import com.backend.givr.shared.entity.projects.InitiativeItem;
import com.backend.givr.shared.enums.ApplicationStatus;
import com.backend.givr.shared.enums.DonationStatus;
import com.backend.givr.shared.enums.VerificationStatus;
import com.backend.givr.shared.exceptions.IllegalOperationException;
import com.backend.givr.shared.repo.DonationRepo;
import com.backend.givr.shared.repo.InitiativeApplicationRepo;
import com.backend.givr.volunteer.entity.Volunteer;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PagedModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InitiativeApplicationService {
    private final InitiativeService initiativeService;
    private final CreatorService creatorService;
    private final ApplicationService applicationService;
    private final InitiativeApplicationRepo repo;
    private final DonationRepo donationRepo;

    @Transactional
    public InitiativeApplication apply(Volunteer volunteer, InitiativeApplicationForm applicationForm){
        if(!volunteer.getVerificationStatus().equals(VerificationStatus.VERIFIED))
            throw new IllegalOperationException("Only verified volunteers can applyForProject to initiatives");

        // A volunteer could volunteer their time, service, or items
        // time and service are the same
        // Those will require the approval of the organization
        // Items do not require organization approval
        InitiativeApplication application = new InitiativeApplication();
        if(applicationForm.getServices()!= null && !applicationForm.getServices().isEmpty()){
            List<InitiativeItem> items = applicationForm.getServices().stream().map((item)->{
                InitiativeItem service = initiativeService.getInitiativeItem(item.resourceId());
                var donation = service.addDonation(volunteer, item.quantity());
                application.getServices().add(donation);
                return service;
            }).toList();

            initiativeService.updateInitiativeItems(items);
        }

        if(applicationForm.getItems()!= null && !applicationForm.getItems().isEmpty()){
            List<InitiativeItem> items = applicationForm.getItems().stream().map((item)->{
                InitiativeItem initiativeItem = initiativeService.getInitiativeItem(item.resourceId());
                var donation = initiativeItem.addDonation(volunteer, item.quantity());
                application.getItems().add(donation);
                return initiativeItem;
            }).toList();

            initiativeService.updateInitiativeItems(items);
        }
        var projectApplication = applicationService.apply(volunteer, applicationForm, volunteer.getEmail());
        application.setProjectApplication(projectApplication);
        return repo.save(application);
    }

    public void updateApplicationStatus(String applicationId, ApplicationStatus status){
        InitiativeApplication application = repo.findById(applicationId)
                .orElseThrow(()-> new EntityNotFoundException("Initiative application does not exist"));

        applicationService.changeApplicationStatus(application.getProjectApplication(), status);
    }

    /*
    * Rules for donation need to be defined*/
    public Donation updateUpdateDonation(DonationUpdateDto donationUpdateDto, String individualId){
        String donationId = donationUpdateDto.donationId();
        DonationStatus status = donationUpdateDto.status();
        Creator creator = creatorService.getCreatorByIndividualId(individualId);
        Donation donation = donationRepo.findByIdAndItemInitiativeCreator(donationId, creator)
                .orElseThrow(()-> new EntityNotFoundException(String.format("Donation with an id %s does not exist", donationId)));

        switch (status){
            case Promised_Accepted -> {
                donation.setStatus(status);
                // notify donor of promise acceptance
            }

            case Promise_Fulfilled -> {
                donation.setStatus(status);
                // Notify user of donation
            }

            case Promise_Rejected -> {
                donation.setStatus(status);
                // Notify donor of promise rejection
            }

            case null, default -> throw new IllegalOperationException(String.format("Updating donation to %s is not permitted", status));
        }

        return donationRepo.save(donation);
    }



    public PagedModel<VolunteerApplicationDto> getInitiativeApplications(Creator individual, PageRequest request) {
        return applicationService.getInitiativeApplications(individual.getIndividual(), request);
    }
}
