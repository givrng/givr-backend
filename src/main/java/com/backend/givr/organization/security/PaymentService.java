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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private VerificationWorker verificationWorker;
    @Autowired
    private VerificationPaymentRepo repo;
    @Autowired
    private GivrTransactionRepo givrTransactionRepo;
    @Autowired
    private OrganizationVerificationSessionRepo verificationSessionRepo;

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
        Organization organization = payment.getOrganization();
        organization.setStatus(VerificationStatus.PENDING);
        String email = detailsService.getEmail(payment.getOrganization());

        payment.updateStatus(TransactionStatus.SUCCESSFUL);
        payment.setAmountPaid(new BigDecimal(amountPaid));
        createGivrTransaction(payment, TransactionType.ORGANIZATION_PAYMENT);

        emailService.sendVerificationStatusUpdate(email, email, ReviewStatus.Pending, "");
        var session = verificationSessionRepo.findByOrganization(organization);

        session.ifPresent(verificationSession -> verificationWorker.verifyContactPersonInformation(verificationSession));
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
