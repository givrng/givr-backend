package com.backend.givr.organization.security;
import com.backend.givr.organization.entity.VerificationPayment;
import com.backend.givr.organization.repo.VerificationPaymentRepo;
import com.backend.givr.shared.dtos.TransactionStatus;
import com.backend.givr.shared.entity.GivrTransaction;
import com.backend.givr.shared.enums.TransactionType;
import com.backend.givr.shared.repo.GivrTransactionRepo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
/**
 * Payment service handles collection and disbursement of payment to monnify and from monnify to verifyMe*/
public class PaymentService {

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private VerificationPaymentRepo repo;
    @Autowired
    private GivrTransactionRepo givrTransactionRepo;

    private void handleSuccessfulTransaction(){

    }

    private void handleFailedTransaction(){

    }

    /**
     * Organization payment was successful*/
    @Transactional
    private void handleSuccessfulPaymentCollection(String payload) throws JsonProcessingException {
        JsonNode node = mapper.readTree(payload);

        JsonNode eventData = node.path("eventData");
        String transactionRef = eventData.path("transactionReference").toString();
        double amountPaid = eventData.path("amountPaid").asDouble();
        double settledAmount = eventData.path("settledAmount").asDouble();

        VerificationPayment payment = repo.findByMerchant(transactionRef).orElseThrow();
        payment.updateStatus(TransactionStatus.SUCCESSFUL);
        payment.setAmountPaid(new BigDecimal(amountPaid));
        payment.setAmountSettled(new BigDecimal(settledAmount));
        createGivrTransaction(payment, TransactionType.ORGANIZATION_PAYMENT);
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
        GivrTransaction transaction = new GivrTransaction(payment.getMerchant(), payment.getTransactionRef(), payment.getAmountPaid(), payment.getAmountSettled(), type);
        givrTransactionRepo.save(transaction);
    }
}
