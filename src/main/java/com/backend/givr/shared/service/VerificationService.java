package com.backend.givr.shared.service;

import com.backend.givr.config.properties.VerificationFeeProperties;
import com.backend.givr.organization.dtos.CheckoutResponse;
import com.backend.givr.organization.dtos.OrganizationUpdateDto;
import com.backend.givr.organization.entity.Organization;
import com.backend.givr.organization.entity.VerificationPayment;
import com.backend.givr.organization.repo.OrganizationRepo;
import com.backend.givr.organization.repo.VerificationPaymentRepo;
import com.backend.givr.organization.security.OrganizationDetailsService;
import com.backend.givr.organization.security.PaymentService;
import com.backend.givr.shared.entity.Location;
import com.backend.givr.shared.entity.OrganizationVerificationSession;

import com.backend.givr.shared.entity.projects.Project;
import com.backend.givr.shared.enums.IDType;
import com.backend.givr.shared.exceptions.DuplicateAccountException;
import com.backend.givr.shared.exceptions.VerificationPaymentProcessedException;
import com.backend.givr.shared.mapper.VerificationMapper;
import com.backend.givr.shared.repo.OrganizationVerificationSessionRepo;
import com.backend.givr.shared.enums.ReviewStatus;
import com.backend.givr.shared.repo.ProjectPaymentReceiptRepo;
import com.backend.givr.volunteer.dtos.VolunteerVerificationDto;
import com.backend.givr.volunteer.entity.Volunteer;
import com.backend.givr.volunteer.entity.VolunteerVerificationSession;
import com.backend.givr.volunteer.service.VolunteerVerificationSessionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationService {

    private final ObjectMapper objectMapper;
    private final PaystackClient merchant;
    private final LocationService locationService;
    private final OrganizationDetailsService detailsService;
    private final OrganizationRepo organizationRepo;
    private final VerificationPaymentRepo paymentRepo;
    private final OrganizationVerificationSessionRepo verificationSessionRepo;
    private final VolunteerVerificationSessionService volunteerVerificationSessionService;
    private final ProjectPaymentReceiptRepo projectPaymentReceiptRepo;
    private final PaymentService paymentService;


    private final VerificationMapper mapper;

    private final VerificationFeeProperties verificationFee;


    public @Nullable Page<OrganizationVerificationSession> getVerificationSessions(ReviewStatus status, PageRequest request) {
        request = request.withSort(Sort.by("createdAt" ).descending());
        if(status == null)
            return verificationSessionRepo.findAll(request);
        return verificationSessionRepo.findByReviewStatus(status, request);
    }

    public OrganizationVerificationSession getVerificationSession(Long sessionId){
        return verificationSessionRepo.findById(sessionId).orElseThrow();
    }

    /**
     * This creates a verification session by saving organization claims,
     * initializing payment and returning checkout url
     * */
    @Transactional
    public CheckoutResponse createVerificationSession(Organization organization, OrganizationUpdateDto organizationUpdateDto) {
        if(organizationUpdateDto.getLocation() == null || organizationUpdateDto.getCacRegNumber() == null || organizationUpdateDto.getCacDocUrl() ==null )
            return null;

        Location location = locationService.createLocation(organizationUpdateDto.getLocation());
        BigDecimal amount = BigDecimal.valueOf(verificationFee.organizationVerificationFee());

        String claimedCacRegNumber = organizationUpdateDto.getCacRegNumber().trim().toUpperCase();
        if(organizationRepo.existsByCacRegNumber(claimedCacRegNumber))
            throw new DuplicateAccountException("An account with this CAC details exist");

        Optional<OrganizationVerificationSession> verificationSession = verificationSessionRepo.findByOrganization(organization);

        if(verificationSession.isPresent()){
            var session = verificationSession.get();
            session.setClaimedLocation(location);
            mapper.updateVerificationSession(organizationUpdateDto, session);
        }else {
            OrganizationVerificationSession session = new OrganizationVerificationSession(organization, organizationUpdateDto);
            session.setClaimedLocation(location);
            verificationSessionRepo.save(session);
        }

        VerificationPayment payment = new VerificationPayment(amount, String.format("Verification of %s account", organization.getOrganizationId()), organization);
        payment.setCallbackUrl(organizationUpdateDto.getCallbackUrl());
        String email = detailsService.getEmail(organization);
        var checkoutUrl = merchant.initializePayment( email, payment);
        paymentRepo.save(payment);
        return new CheckoutResponse(checkoutUrl);
    }



    @Transactional
    public CheckoutResponse createVolunteerVerificationSession(VolunteerVerificationDto verificationDto, Volunteer volunteer){

        var amount = verificationFee.individualVerificationFee();

        IDType type = verificationDto.idType();
        String number = verificationDto.idNumber().toUpperCase().trim();
        Optional<VolunteerVerificationSession> optionalVerificationSession = volunteerVerificationSessionService.findByIdTypAndNumber(type, number);

        String description = String.format("Verification of %s account", volunteer.getVolunteerId());
        if(optionalVerificationSession.isPresent() && volunteer.getVerificationStatus() != null){
            throw new VerificationPaymentProcessedException("Verification has been paid for, await verification update from admin");
        }else if(optionalVerificationSession.isPresent())
            return processCheckoutUrl(volunteer, amount,description, verificationDto.callbackUrl());

        var volunteerSession = volunteerVerificationSessionService.getVerificationSessionByVolunteer(volunteer);
        VolunteerVerificationSession session;
        if(volunteerSession.isPresent()){
            session = volunteerSession.get();
            session.update(verificationDto);
        }else session = new VolunteerVerificationSession(verificationDto, volunteer);

        volunteerVerificationSessionService.save(session);

        return processCheckoutUrl(volunteer, amount, description, verificationDto.callbackUrl());
    }

    private CheckoutResponse processCheckoutUrl(Volunteer volunteer, Double amount, String description, String callbackUrl){

        VerificationPayment payment = createVerificationPayment(amount,  description,
                volunteer);

        payment.setCallbackUrl(callbackUrl);
        String email = volunteer.getEmail();
        var checkout = merchant.initializePayment(email, payment);
        paymentRepo.save(payment);
        return new CheckoutResponse(checkout);
    }

    private VerificationPayment createVerificationPayment(Double amount, String description, Object reason){
        if( reason instanceof Volunteer volunteer)
            return new VerificationPayment(BigDecimal.valueOf(amount), description, volunteer);
        else if(reason instanceof Organization organization)
            return new VerificationPayment(BigDecimal.valueOf(amount), description, organization);
        else if (reason instanceof Project project)
            return new VerificationPayment(BigDecimal.valueOf(amount), description, project);
        throw new IllegalArgumentException("Unknown Verification reason");
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
