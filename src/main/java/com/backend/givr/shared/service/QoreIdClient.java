package com.backend.givr.shared.service;

import com.backend.givr.shared.entity.OrganizationVerificationSession;
import com.backend.givr.shared.enums.AuthHeader;
import com.backend.givr.shared.enums.IDType;
import com.backend.givr.shared.enums.VerificationStatus;
import com.backend.givr.shared.exceptions.VerificationFailedException;
import com.backend.givr.shared.interfaces.VerificationClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;

/**
 * QoreIdClient is an implementation of QoreId Api for Givr organization verification
 *
 **/
@Slf4j
@Service
public class QoreIdClient implements VerificationClient {
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private ObjectMapper mapper;

    // Provide environment variables for injected properties and decorate class as a service
    @Value("${qore.id.clientId}")
    private String clientId;
    @Value("${qore.id.clientSecret}")
    private String clientSecret;
    @Value("${qore.api.baseUrl}")
    private String BASEURL;
    private AuthHeader authHeader;
    private static String accessToken;
    private static LocalDateTime expiresAt;

    private final Logger logger = LoggerFactory.getLogger(QoreIdClient.class);

    // Make sure to authenticate before verifying
    public void authenticate() throws JsonProcessingException {
        String path = "token";
        try{
            var response = fetch(HttpMethod.POST, path, Map.of("clientId", clientId, "secret", clientSecret), String.class);

                JsonNode node = mapper.readTree(response.getBody());

                accessToken = node.path("accessToken").asText();
                this.authHeader = AuthHeader.valueOf(node.path("tokenType").asText());
                String expires = node.path("expiresIn").asText();
                expiresAt = LocalDateTime.now().plusSeconds(Long.parseLong(expires));

        }catch (RestClientException ignored){
            logger.error("Failed to authenticate with QoreId. Failed to get accessToken");
        }
    }


    // Check for the expiration or the presence of access tokens before attempting to verify
    public void verify(OrganizationVerificationSession session) throws JsonProcessingException {
        String path = switch (session.getIdType()){
            case vNIN -> String.format("v1/ng/identities/virtual-nin/%s", session.getIdNumber());

            case DL -> String.format("v1/ng/identities/drivers-license/%s", session.getIdNumber());

            case VOTER_CARD -> String.format("v1/ng/identities/vin/%s", session.getIdNumber());

            case PASSPORT -> String.format("v1/ng/identities/passport/%s", session.getIdNumber());

            case null, default -> "";
        };

        if(expiresAt == null || accessToken == null)
            authenticate();

        if(expiresAt.isAfter(LocalDateTime.now()))
            authenticate();

        Map<String, Object> payload = session.getIdType() == IDType.VOTER_CARD? Map.of("idNumber", session.getIdNumber(), "firstName", session.getContactFirstname(),"lastName", session.getContactLastname(), "dateOfBirth", session.getDateOfBirth()):
                Map.of("idNumber", session.getIdNumber(), "firstName", session.getContactFirstname(),"lastName", session.getContactLastname());

        try{
            var response = fetch(HttpMethod.POST, path, payload, String.class);

            if(response.getStatusCode().is2xxSuccessful()){
                session.setVerificationStatus(VerificationStatus.AUTOMATIC_VERIFICATION_SUCCEEDED);
            }

        }catch (HttpClientErrorException e){
            String responseBody = e.getResponseBodyAsString();
            System.out.println(responseBody);
            Map<String, Object> errBody= mapper.readValue(responseBody, Map.class);
            String msg = (String) errBody.get("message");

            logger.error("Failed to verify user because {}", msg);

            session.setVerificationStatus(VerificationStatus.AUTOMATIC_VERIFICATION_FAILED);
            session.setRemark(String.format("%s", msg));
        }
    }

    private <T> ResponseEntity<T> fetch(HttpMethod requestType, String path, Object body, Class<T> responseType){
        var endpoint = BASEURL + path;
        HttpHeaders headers = new HttpHeaders();

        if( accessToken != null)
            headers.add("Authorization", authHeader.toString() + " " + accessToken);

        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> entity = new HttpEntity<>(body, headers);

        return restTemplate.exchange(endpoint, requestType, entity, responseType);
    }
    
}
