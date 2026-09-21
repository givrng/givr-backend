package com.backend.givr.organization.security;
import com.backend.givr.organization.entity.Organization;
import com.backend.givr.organization.entity.VerificationPayment;
import com.backend.givr.organization.repo.VerificationPaymentRepo;
import com.backend.givr.shared.dtos.TransactionStatus;
import com.backend.givr.shared.email.EmailService;
import com.backend.givr.shared.entity.GivrTransaction;
import com.backend.givr.shared.enums.ReviewStatus;
import com.backend.givr.shared.enums.TransactionType;
import com.backend.givr.shared.enums.VerificationStatus;
import com.backend.givr.shared.repo.GivrTransactionRepo;
import com.backend.givr.shared.repo.OrganizationVerificationSessionRepo;
import com.backend.givr.shared.service.VerificationWorker;
import com.backend.givr.volunteer.entity.Volunteer;
import com.backend.givr.volunteer.service.VolunteerVerificationSessionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;


/**
 * Payment service handles verification of payment to PayStack, initiates verifications, and notifies users*/
@Service
@Slf4j
public class PaymentService {

    private final Logger logger = LoggerFactory.getLogger(PaymentService.class);
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private OrganizationDetailsService detailsService;
    @Autowired
    private EmailService emailService;
    @Autowired
    private EntityManager manager;

    @Autowired
    private VerificationWorker verificationWorker;
    @Autowired
    private VerificationPaymentRepo repo;
    @Autowired
    private GivrTransactionRepo givrTransactionRepo;
    @Autowired
    private OrganizationVerificationSessionRepo verificationSessionRepo;

    @Autowired
    private VolunteerVerificationSessionService volunteerVerificationSessionService;
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
        String transactionRef = eventData.path("reference").asText().trim();
        double amountPaid = eventData.path("amount").asDouble();
        logger.info("MerchantId {}", transactionRef);

        VerificationPayment payment = repo.findByMerchantRefId(transactionRef).orElseThrow();
        payment.updateStatus(TransactionStatus.SUCCESSFUL);
        payment.setAmountPaid(new BigDecimal(amountPaid));
        String email;

        if(transactionRef.startsWith("ORG")){
            Organization organization = manager.getReference(payment.getOrganization());
            organization.setStatus(VerificationStatus.PENDING);
            email = detailsService.getEmail(payment.getOrganization());
            var session = verificationSessionRepo.findByOrganization(organization);
            createGivrTransaction(payment, TransactionType.ORGANIZATION_PAYMENT);
            session.ifPresent(verificationSession -> verificationWorker.verifyContactPersonInformation(verificationSession));
        } else {
            Volunteer volunteer = manager.getReference(payment.getVolunteer());
            volunteer.setVerificationStatus(VerificationStatus.PENDING);
            email = volunteer.getEmail();

            var session = volunteerVerificationSessionService.getVerificationSessionByVolunteer(volunteer);
            createGivrTransaction(payment, TransactionType.VOLUNTEER_PAYMENT);

            session.ifPresent(verificationSession->verificationWorker.verifyVolunteer(verificationSession));
        }
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
}
