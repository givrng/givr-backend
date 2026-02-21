package com.backend.givr.shared.service;

import com.backend.givr.organization.entity.VerificationPayment;
import com.backend.givr.shared.dtos.InitTransaction;
import com.backend.givr.shared.dtos.PaymentInitResponse;
import com.backend.givr.shared.enums.CurrencyCode;
import com.backend.givr.shared.enums.PaymentMethod;
import com.backend.givr.shared.exceptions.FailedToInitiatePaymentException;
import com.backend.givr.shared.interfaces.PaymentMerchant;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.constraints.Email;
import org.apache.coyote.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;


public class MonnifyClient implements PaymentMerchant {

    private enum AuthHeader{
        Basic, Bearer
    }
    private final Logger log = LoggerFactory.getLogger(MonnifyClient.class);
    private final String APIKEY;
    private final String SECRET_KEY;
    private final String BASEURL;
    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private RestTemplate requestHandler;

    public MonnifyClient(@Value("${monnify.api.key}") String apiKey, @Value("${monnify.secret.key}") String secretKey, @Value("${monnify.base.url}") String url){
        this.APIKEY = apiKey;
        this.SECRET_KEY = secretKey;
        this.BASEURL = url;
    }

    @Override
    public String authenticate() throws JsonProcessingException, BadRequestException {
        String authToken = Base64.getEncoder().encodeToString((APIKEY+":"+ SECRET_KEY).getBytes(StandardCharsets.UTF_8));
        var path = "/api/v1/auth/login";

        var response = fetch(HttpMethod.POST, AuthHeader.Basic, path, authToken, " ", String.class);
        if(response.getStatusCode() == HttpStatus.OK){
            JsonNode node = mapper.readTree(response.getBody());
            var token = node.path("responseBody").path("accessToken").asText();
            return token;
        }else throw new BadRequestException("Failed to fetch access token");

    }

    private <T> ResponseEntity<T> fetch(HttpMethod requestType, AuthHeader authHeader, String path, String authToken, Object body, Class<T> responseType){
        var endpoint = BASEURL + path;
        HttpHeaders headers = new HttpHeaders();

        if(authHeader != null && authToken != null)
            headers.add("Authorization", authHeader.toString() + " " + authToken);

        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> entity = new HttpEntity<>(body, headers);

        return requestHandler.exchange(endpoint, requestType, entity, responseType);
    }

    @Override
    public PaymentInitResponse initializePayment(String clientNumber, @Email String email, VerificationPayment payment) throws BadRequestException {
        var path = "/api/v1/merchant/transactions/init-transaction";

        InitTransaction initializedTransaction = new InitTransaction(payment.getAmount(), clientNumber, email, payment.getTransactionRef(), payment.getDescription(), CurrencyCode.NGN,"", List.of(PaymentMethod.CARD, PaymentMethod.ACCOUNT_TRANSFER, PaymentMethod.USSD));
        try{
            var accessToken = authenticate();
            var response = fetch(HttpMethod.POST, AuthHeader.Bearer, path, accessToken, initializedTransaction, PaymentInitResponse.class);
            if(response.getStatusCode().is2xxSuccessful()){
                PaymentInitResponse initResponse = response.getBody();
                if(initResponse != null)
                    payment.setMerchantId(initResponse.responseBody().transactionReference());
                return initResponse;
            }else throw new FailedToInitiatePaymentException("Failed to initialize transaction");
        } catch (IOException e) {
            log.error("Failed to read response from Monnify");
        }
        return null;
    }


}
