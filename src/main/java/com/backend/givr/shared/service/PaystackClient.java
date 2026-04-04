package com.backend.givr.shared.service;

import com.backend.givr.organization.entity.VerificationPayment;
import com.backend.givr.shared.enums.AuthHeader;
import com.backend.givr.shared.exceptions.FailedToInitiatePaymentException;
import com.backend.givr.shared.interfaces.PaymentMerchant;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.xml.bind.DatatypeConverter;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class PaystackClient implements PaymentMerchant {
    @Value("${paystack.key.secret}")
    private String secretKey;
    @Value("${paystack.base.url}")
    private String baseUrl;
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private RestTemplate requestHandler;

    private final Logger logger = LoggerFactory.getLogger(PaystackClient.class);

    @Override
    public String authenticate() throws JsonProcessingException, BadRequestException {
        return "";
    }


    @Override
    public String initializePayment( String email, VerificationPayment verificationPayment) {
        Map<String, Object> payload = Map.of(
                "amount", verificationPayment.getAmount().multiply(BigDecimal.valueOf(100)),
                "email", email,
                "channels", List.of("card", "bank", "ussd", "bank_transfer"),
                "currency", "NGN"
        );
        ResponseEntity<String> response = fetch(HttpMethod.POST, "/transaction/initialize", payload, String.class);

        try{
            if(response.getStatusCode().is2xxSuccessful()){
                JsonNode node = mapper.readTree(response.getBody()).path("data");
                String checkoutUrl = node.path("authorization_url").asText();
                String merchantRef = node.path("reference").asText();
                verificationPayment.setMerchantRefId(merchantRef);
                return checkoutUrl;
            }else throw new FailedToInitiatePaymentException("Failed to initialize paystack payment");
        }catch ( JsonProcessingException e){
            logger.error("Failed to parse paystack response, {}", e.getLocalizedMessage());
        }
        return null;
    }

    private <T> ResponseEntity<T> fetch(HttpMethod requestType, String path, Object body, Class<T> responseType){
        var endpoint = baseUrl + path;
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", AuthHeader.Bearer.toString() + " " + secretKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> entity = new HttpEntity<>(body, headers);

        return requestHandler.exchange(endpoint, requestType, entity, responseType);
    }

    public boolean hashIsValid(String response, String xpaystackSignature)  {
        String result = "";
        String HMAC_SHA512 = "HmacSHA512";

         //put in the request's header value for x-paystack-signature
        try{
            byte [] byteKey = secretKey.getBytes("UTF-8");
            SecretKeySpec keySpec = new SecretKeySpec(byteKey, HMAC_SHA512);
            Mac sha512_HMAC = Mac.getInstance(HMAC_SHA512);
            sha512_HMAC.init(keySpec);
            byte [] mac_data = sha512_HMAC.
                    doFinal(response.getBytes("UTF-8"));

            result = DatatypeConverter.printHexBinary(mac_data);
            return result.toLowerCase().equals(xpaystackSignature);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
