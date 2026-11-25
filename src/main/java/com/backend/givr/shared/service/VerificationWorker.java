package com.backend.givr.shared.service;

import com.backend.givr.config.properties.VerificationFeeProperties;
import com.backend.givr.organization.dtos.CheckoutResponse;
import com.backend.givr.organization.entity.Organization;
import com.backend.givr.organization.entity.VerificationPayment;
import com.backend.givr.organization.repo.VerificationPaymentRepo;
import com.backend.givr.shared.dtos.PublishProjectDto;
import com.backend.givr.shared.entity.Creator;
import com.backend.givr.shared.entity.OrganizationVerificationSession;
import com.backend.givr.shared.entity.ProjectPaymentReceipt;
import com.backend.givr.shared.entity.projects.Initiative;
import com.backend.givr.shared.entity.projects.Project;
import com.backend.givr.shared.interfaces.SecurityDetails;
import com.backend.givr.shared.repo.OrganizationVerificationSessionRepo;
import com.backend.givr.shared.repo.ProjectPaymentReceiptRepo;
import com.backend.givr.volunteer.entity.Volunteer;
import com.backend.givr.volunteer.entity.VolunteerVerificationSession;
import com.backend.givr.volunteer.repo.VolunteerRepo;
import com.backend.givr.volunteer.repo.VolunteerVerificationSessionRepo;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;


@Service
@Slf4j
@RequiredArgsConstructor
public class VerificationWorker {

    private final QoreIdClient verificationClient;
    private final OrganizationVerificationSessionRepo organizationVerificationSessionRepo;
    private final VolunteerRepo volunteerRepo;
    private final VolunteerVerificationSessionRepo volunteerVerificationSessionRepo;
    private final CreatorService creatorService;
    private final VerificationPaymentRepo paymentRepo;

    private final PaystackClient merchant;
    private final VerificationFeeProperties verificationFee;
    private final ProjectPaymentReceiptRepo projectPaymentReceiptRepo;

    private final Logger logger = LoggerFactory.getLogger(VerificationWorker.class);
//
//    value = Exception.class,
//    maxAttempts = 3,
//    backoff = @Backoff(delay = 2000)
    @Async
    @Retryable(
            retryFor = Exception.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000)
    )
    public void verifyContactPersonInformation(OrganizationVerificationSession verificationSession){
        try{
            verificationClient.verifyOrganization(verificationSession);
        } catch (JsonProcessingException e) {
            logger.error("Verification for {} failed because of {}", verificationSession.getSessionId(), e.getLocalizedMessage());
        }finally {
            organizationVerificationSessionRepo.save(verificationSession);
        }
    }

    @Async
    @Retryable(
            retryFor = Exception.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000)
    )
    public void verifyVolunteer(VolunteerVerificationSession verificationSession) {
        try{
            verificationClient.verifyVolunteer(verificationSession);
        } catch (JsonProcessingException e) {
            logger.error("Volunteer verification for {} failed because of {}", verificationSession.getVerificationSessionId(), e.getLocalizedMessage());
        }finally {
            volunteerRepo.save(verificationSession.getVolunteer());
            volunteerVerificationSessionRepo.save(verificationSession);
        }
    }

    @Transactional
    public CheckoutResponse createInitiativePayment(Initiative initiative, SecurityDetails details, PublishProjectDto publishProjectDto){
        if(initiative == null || details == null)
            return null;

        var fee = verificationFee.initiativeCreationFee();

        Creator creator = creatorService.getCreatorByIndividualId(details.getId());
        String callbackUrl = publishProjectDto.callbackUrl();
        Optional<ProjectPaymentReceipt> optionalReceipt = projectPaymentReceiptRepo.findByCreatorAndProject(creator, initiative);

        String description = String.format("Initiative Creation Fee for Initiative-%s", initiative.getProjectId());

        VerificationPayment payment = createVerificationPayment(fee, description, initiative);
        payment.setCallbackUrl(callbackUrl);
        paymentRepo.save(payment);

        String email = details.getUsername();

        if(optionalReceipt.isPresent()){
            ProjectPaymentReceipt paymentReceipt = optionalReceipt.get();
            switch (paymentReceipt.getStatus()){
                case PENDING, FAILED -> {
                    paymentReceipt.setPayment(payment);
                    return processCheckoutUrl(payment, email);
                }
            }
        }

        ProjectPaymentReceipt paymentReceipt = new ProjectPaymentReceipt(creator, initiative, payment);
        projectPaymentReceiptRepo.save(paymentReceipt);

        return processCheckoutUrl(payment, email);
    }

    private CheckoutResponse processCheckoutUrl(VerificationPayment payment, String email){
        var checkout = merchant.initializePayment(email, payment);
        paymentRepo.save(payment);
        return new CheckoutResponse(checkout);
    }

    private VerificationPayment createVerificationPayment(Double amount, String description, Object reason){
        if( reason instanceof Volunteer volunteer)
            return new VerificationPayment(BigDecimal.valueOf(amount), description, volunteer);
        else if(reason instanceof Organization organization)
            return new VerificationPayment(BigDecimal.valueOf(amount), description, organization);
        else if (reason instanceof Project project)
            return new VerificationPayment(BigDecimal.valueOf(amount), description, project);
        throw new IllegalArgumentException("Unknown Verification reason");
    }
}
