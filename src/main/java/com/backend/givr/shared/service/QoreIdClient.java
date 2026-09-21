package com.backend.givr.shared.service;

import com.backend.givr.shared.entity.OrganizationVerificationSession;
import com.backend.givr.shared.enums.AuthHeader;
import com.backend.givr.shared.enums.IDType;
import com.backend.givr.shared.enums.VerificationStatus;
import com.backend.givr.shared.exceptions.VerificationFailedException;
import com.backend.givr.shared.interfaces.VerificationClient;
import com.backend.givr.volunteer.entity.VolunteerVerificationSession;
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


    // Check for the expiration or the presence of access tokens before attempting to verifyOrganization
    public void verifyOrganization(OrganizationVerificationSession session) throws JsonProcessingException {
        String path = switch (session.getIdType()){
            case NIN -> String.format("v1/ng/identities/nin-premium/%s", session.getIdNumber());

            case DL -> String.format("v1/ng/identities/drivers-license/%s", session.getIdNumber());

            case VOTER_CARD -> String.format("v1/ng/identities/vin/%s", session.getIdNumber());

            case PASSPORT -> String.format("v1/ng/identities/passport/%s", session.getIdNumber());

            case null, default -> "";
        };

        if(session.getIdNumber()==null || session.getIdNumber().isEmpty())
            return;

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

            logger.error("Failed to verifyOrganization user because {}", msg);

            session.setVerificationStatus(VerificationStatus.AUTOMATIC_VERIFICATION_FAILED);
            session.setRemark(String.format("%s", msg));
        }
    }

    public void verifyVolunteer(VolunteerVerificationSession session) throws JsonProcessingException {
        String path = switch (session.getIdType()){
            case NIN -> String.format("v1/ng/identities/nin-premium/%s", session.getIdNumber());

            case DL -> String.format("v1/ng/identities/drivers-license/%s", session.getIdNumber());

            case VOTER_CARD -> String.format("v1/ng/identities/vin/%s", session.getIdNumber());

            case PASSPORT -> String.format("v1/ng/identities/passport/%s", session.getIdNumber());

            case null, default -> "";
        };

        if(session.getIdNumber()==null || session.getIdNumber().isEmpty())
            return;

        if(expiresAt == null || accessToken == null)
            authenticate();

        if(expiresAt.isAfter(LocalDateTime.now()))
            authenticate();

        Map<String, Object> payload = session.getIdType() == IDType.VOTER_CARD? Map.of("idNumber", session.getIdNumber(), "firstname", session.getFirstname(),"lastname", session.getLastname(), "middlename", session.getMiddleName()==null?"":session.getMiddleName(), "dateOfBirth", session.getDateOfBirth()):
                Map.of( "firstname", session.getFirstname(),"lastname", session.getLastname(), "middlename", session.getMiddleName()==null?"":session.getMiddleName());

        try{
            var response = fetch(HttpMethod.POST, path, payload, String.class);

            if(response.getStatusCode().is2xxSuccessful()){
                session.setVerificationStatus(VerificationStatus.VERIFIED);
                session.getVolunteer().setVerificationStatus(VerificationStatus.VERIFIED);
            }

        }catch (HttpClientErrorException e){
            String responseBody = e.getResponseBodyAsString();
            System.out.println(responseBody);
            Map<String, Object> errBody= mapper.readValue(responseBody, Map.class);
            String msg = (String) errBody.get("message");

            logger.info(mapper.writeValueAsString(payload));
            logger.info("Request path: {}", path);
            logger.error("Failed to verify volunteer user because {}", msg);

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
