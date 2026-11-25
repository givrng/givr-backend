package com.backend.givr.organization.security;
import com.backend.givr.organization.entity.Organization;
import com.backend.givr.organization.entity.VerificationPayment;
import com.backend.givr.organization.repo.VerificationPaymentRepo;
import com.backend.givr.shared.dtos.TransactionStatus;
import com.backend.givr.shared.email.EmailService;
import com.backend.givr.shared.entity.GivrTransaction;
import com.backend.givr.shared.entity.ProjectPaymentReceipt;
import com.backend.givr.shared.entity.projects.Initiative;
import com.backend.givr.shared.entity.projects.Project;
import com.backend.givr.shared.enums.ReviewStatus;
import com.backend.givr.shared.enums.TransactionType;
import com.backend.givr.shared.enums.VerificationStatus;
import com.backend.givr.shared.exceptions.UnknownPaymentPrefixException;
import com.backend.givr.shared.repo.GivrTransactionRepo;
import com.backend.givr.shared.repo.OrganizationVerificationSessionRepo;
import com.backend.givr.shared.repo.ProjectPaymentReceiptRepo;
import com.backend.givr.shared.service.InitiativeService;
import com.backend.givr.shared.service.VerificationWorker;
import com.backend.givr.volunteer.entity.Volunteer;
import com.backend.givr.volunteer.service.VolunteerVerificationSessionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;


/**
 * Payment service handles verification of payment to PayStack, initiates verifications, and notifies users*/
@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService {

    private final ObjectMapper mapper;
    private final OrganizationDetailsService detailsService;
    private final EmailService emailService;
    private final EntityManager manager;

    private final VerificationWorker verificationWorker;
    private final VerificationPaymentRepo repo;
    private final GivrTransactionRepo givrTransactionRepo;
    private final OrganizationVerificationSessionRepo verificationSessionRepo;

    private final InitiativeService initiativeService;
    private final ProjectPaymentReceiptRepo projectPaymentRepo;
    private final VolunteerVerificationSessionService volunteerVerificationSessionService;

    private void handleSuccessfulTransaction(){

    }

    private void handleFailedTransaction(){

    }

    /**
     * Organization payment was successful*/
    @Transactional
    public void handleSuccessfulPaymentCollection(String payload) throws JsonProcessingException {
        JsonNode node = mapper.readTree(payload);
        JsonNode eventData = node.path("data");
        String merchantRef = eventData.path("reference").asText().trim();
        double amountPaid = eventData.path("amount").asDouble();
        log.info("MerchantId {}", merchantRef);

        VerificationPayment payment = repo.findByMerchantRefId(merchantRef).orElseThrow();
        String transactionRef = payment.getTransactionRef();
        payment.updateStatus(TransactionStatus.SUCCESSFUL);
        payment.setAmountPaid(new BigDecimal(amountPaid));
        String email;

        if(transactionRef.startsWith("ORG")){
            Organization organization = manager.getReference(payment.getOrganization());
            organization.setStatus(VerificationStatus.PENDING);
            email = detailsService.getEmail(payment.getOrganization());
            var session = verificationSessionRepo.findByOrganization(organization);
            createGivrTransaction(payment, TransactionType.ORGANIZATION_PAYMENT);
            session.ifPresent(verificationWorker::verifyContactPersonInformation);
        } else if (transactionRef.startsWith("VOL")) {
            Volunteer volunteer = manager.getReference(payment.getVolunteer());
            volunteer.setVerificationStatus(VerificationStatus.PENDING);
            email = volunteer.getEmail();

            var session = volunteerVerificationSessionService.getVerificationSessionByVolunteer(volunteer);
            createGivrTransaction(payment, TransactionType.VOLUNTEER_PAYMENT);

            session.ifPresent(verificationWorker::verifyVolunteer);
        } else if (transactionRef.startsWith("Project")) {
            Project project = (Project) Hibernate.unproxy(payment.getProject());

             if (project instanceof Initiative initiative){
                 email = project.getCreator().getIndividual().getEmail();
                 confirmAndPublish(payment,initiative);
             } else
                 email = detailsService.getEmail(project.getCreator().getOrganization());

             createGivrTransaction(payment, TransactionType.INITIATIVE_CREATION);
        } else throw new UnknownPaymentPrefixException(String.format("Unknown transactionRef: %s", transactionRef));

        emailService.sendVerificationStatusUpdate(email, email, ReviewStatus.Pending, "");
    }

    /**
     * VerifyMe was paid successfully for organization profile verification*/
    private void handleSuccessfulDisbursement(){

    }

    /**
     * Insufficient amount in wallet to process transaction to VerifyMe*/
    private void handleFailedDisbursement(){

    }

    private void createGivrTransaction(VerificationPayment payment, TransactionType type){
        GivrTransaction transaction = new GivrTransaction(payment.getMerchant(), payment.getTransactionRef(), payment.getAmountPaid(), type);
        givrTransactionRepo.save(transaction);
    }

    private void confirmAndPublish(VerificationPayment verificationPayment, Initiative initiative){
        Optional<ProjectPaymentReceipt> optionalReceipt = projectPaymentRepo.findByPayment(verificationPayment);

        if(optionalReceipt.isEmpty()) {
            log.error("Processed payment for verification-payment-{} does not have a corresponding projectPaymentReceipt", verificationPayment.getPaymentId());
            return;
        }

        ProjectPaymentReceipt receipt = optionalReceipt.get();
        receipt.setStatus(verificationPayment.getStatus());
        initiative.setCreationReceipt(projectPaymentRepo.save(receipt));
        initiativeService.publishProject(initiative, initiative.getCreator());
    }
}
