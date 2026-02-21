package com.backend.givr.shared.interfaces;

import com.backend.givr.organization.entity.VerificationPayment;
import com.backend.givr.shared.dtos.PaymentInitResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.constraints.Email;
import org.apache.coyote.BadRequestException;

public interface PaymentMerchant {
    /**
     * Establish a connection between server and payment provider
     * @return Access token from payment merchant**/
    public String authenticate() throws JsonProcessingException, BadRequestException;

    /**
     * Open a transaction with the merchant**/
    public PaymentInitResponse initializePayment(String clientNumber, @Email String email, VerificationPayment verificationPayment) throws BadRequestException;

}
