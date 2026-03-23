package com.backend.givr.shared.service;

import com.backend.givr.organization.dtos.CheckoutResponse;
import com.backend.givr.organization.dtos.OrganizationUpdateDto;
import com.backend.givr.organization.entity.Organization;
import com.backend.givr.organization.entity.VerificationPayment;
import com.backend.givr.organization.repo.VerificationPaymentRepo;
import com.backend.givr.organization.security.OrganizationDetailsService;
import com.backend.givr.organization.security.PaymentService;
import com.backend.givr.shared.entity.OrganizationVerificationSession;
import com.backend.givr.shared.mapper.VerificationMapper;
import com.backend.givr.shared.repo.OrganizationVerificationSessionRepo;
import com.backend.givr.shared.email.EmailService;
import com.backend.givr.shared.enums.ReviewStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class VerificationService {

    @Autowired
    private EmailService emailService;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private PaystackClient merchant;

    @Autowired
    private OrganizationDetailsService detailsService;
    @Autowired
    private VerificationPaymentRepo paymentRepo;
    @Autowired
    private OrganizationVerificationSessionRepo verificationSessionRepo;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private VerificationMapper mapper;

    @Value("${givr.verification.amount}")
    private Double amount;

    private final Logger logger = LoggerFactory.getLogger(VerificationService.class);

    public @Nullable List<OrganizationVerificationSession> getVerificationSessions(ReviewStatus status) {
        if(status == null)
            return verificationSessionRepo.findAll();
        
        Sort sort = Sort.by("createdAt" ).descending();
        return verificationSessionRepo.findByReviewStatus(status, sort);
    }


    /**
     * This creates a verification session by saving organization claims,
     * initializing payment and returning checkout url
     * */
    @Transactional
    public CheckoutResponse createVerificationSession(Organization organization, OrganizationUpdateDto organizationUpdateDto){
        if(organizationUpdateDto.getLocation() == null || organizationUpdateDto.getCacRegNumber() == null || organizationUpdateDto.getCacDocUrl() ==null || organizationUpdateDto.getContactVerification() == null)
            return null;

        Optional<OrganizationVerificationSession> verificationSession = verificationSessionRepo.findByOrganization(organization);

        if(verificationSession.isPresent()){
            var session = verificationSession.get();
            mapper.updateVerificationSess(organizationUpdateDto, session);
        }else {
            OrganizationVerificationSession session = new OrganizationVerificationSession(organization, organizationUpdateDto);
            verificationSessionRepo.save(session);
        }

        VerificationPayment payment = new VerificationPayment(BigDecimal.valueOf(amount), String.format("Verification of %s account", organization.getOrganizationId()), organization);

        String email = detailsService.getEmail(organization);
        var checkoutUrl = merchant.initializePayment( email, payment);
        paymentRepo.save(payment);
        return new CheckoutResponse(checkoutUrl);
    }

    @Async
    public void initiateVerification(String body){
        try{
            JsonNode node = objectMapper.readTree(body);
            String event = node.path("event").asText();
            JsonNode data = node.path("data");
            String status = data.path("status").asText();

            if("charge.success".equals(event) && "success".equals(status)){
                paymentService.handleSuccessfulPaymentCollection(body);

            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
