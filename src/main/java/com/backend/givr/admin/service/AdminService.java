package com.backend.givr.admin.service;

import com.backend.givr.admin.dtos.AdminAuthDto;
import com.backend.givr.admin.dtos.ReviewDto;
import com.backend.givr.admin.dtos.ReviewResponseDto;
import com.backend.givr.admin.entity.Admin;
import com.backend.givr.admin.entity.AdminDetails;
import com.backend.givr.admin.entity.Review;
import com.backend.givr.admin.enums.AdminRole;
import com.backend.givr.admin.enums.SessionType;
import com.backend.givr.admin.mapper.AdminMapper;
import com.backend.givr.admin.repos.AdminRepo;
import com.backend.givr.admin.repos.ReviewRepo;
import com.backend.givr.organization.entity.Organization;
import com.backend.givr.organization.service.OrganizationService;
import com.backend.givr.shared.dtos.VerificationSessionDto;
import com.backend.givr.shared.dtos.VolunteerVerificationSessionDto;
import com.backend.givr.shared.email.EmailService;
import com.backend.givr.shared.entity.OrganizationVerificationSession;
import com.backend.givr.shared.enums.AccountType;
import com.backend.givr.shared.enums.OtpPurpose;
import com.backend.givr.shared.enums.VerificationStatus;
import com.backend.givr.shared.exceptions.DuplicateAccountException;
import com.backend.givr.shared.interfaces.SecurityDetails;
import com.backend.givr.shared.otp.OTPService;
import com.backend.givr.shared.service.VerificationService;
import com.backend.givr.shared.enums.ReviewStatus;
import com.backend.givr.shared.service.VerificationWorker;
import com.backend.givr.volunteer.entity.VolunteerVerificationSession;
import com.backend.givr.volunteer.service.VolunteerVerificationSessionService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.constraints.Email;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PagedModel;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

import static com.backend.givr.shared.enums.ReviewStatus.Approved;

@Service
public class AdminService {
    @Autowired
    private AdminRepo repo;

    @Autowired
    private AdminDetailsService service;
    @Autowired
    private ReviewRepo reviewRepo;

    @Autowired
    private OrganizationService organizationService;
    @Autowired
    private AdminMapper mapper;
    @Autowired
    private EntityManager manager;

    @Autowired
    private EmailService emailService;
    @Autowired
    private OTPService otpService;
    @Autowired
    private VerificationService verificationService;
    @Autowired
    private VerificationWorker verificationWorker;
    @Autowired
    private VolunteerVerificationSessionService volunteerVerificationSessionService;

    @Async
    public void requestOtp(String email){
        emailService.sendOtpTo(email, AccountType.ADMIN, OtpPurpose.ADMIN_AUTH);
    }

    public AdminDetails validateOtp(AdminAuthDto authDto){
        otpService.verifyOtp(authDto.email(), authDto.otp(), AccountType.ADMIN, OtpPurpose.ADMIN_AUTH);
        return service.loadUserByUsername(authDto.email());
    }

    public Admin getAdminByEmail(@Email String email){
        return repo.findByEmail(email).orElseThrow(()->new EntityNotFoundException(String.format("Admin with %s does not exist", email)));
    }

    public @Nullable PagedModel<VerificationSessionDto> getVerificationSession(ReviewStatus status, PageRequest request) {
        Page<OrganizationVerificationSession> pageSessions = verificationService.getVerificationSessions(status, request);

        PageImpl<VerificationSessionDto> verificationImpl = new PageImpl<>(Objects.requireNonNull(pageSessions).getContent().stream().map(VerificationSessionDto::new).toList(),
                pageSessions.getPageable(), pageSessions.getTotalElements());
        return new PagedModel<>(verificationImpl);
    }

    public PagedModel<VolunteerVerificationSessionDto> getVolunteerVerificationSession(ReviewStatus status, PageRequest request){
        Page<VolunteerVerificationSession> pageSessions = volunteerVerificationSessionService.getVerificationSessions(status, request);

        PageImpl<VolunteerVerificationSessionDto> verificationImpl = new PageImpl<>(Objects.requireNonNull(pageSessions).getContent().stream().map(VolunteerVerificationSessionDto::new).toList(),
                pageSessions.getPageable(), pageSessions.getTotalElements());
        return new PagedModel<>(verificationImpl);
    }

    public void createAdmin(String email){
        Admin admin = Admin.createAdmin(email, AdminRole.SUPER_ADMIN);
        repo.save(admin);
    }

    public boolean adminExists(String email){
        return repo.existsByEmail(email);
    }
    /*
    * Does not support concurrent reviews
    * Simple mvp review function
    * Admin submits review, organization is notified on their account update*/
    @Transactional
    public ReviewResponseDto createReview(ReviewDto reviewDto, SecurityDetails admin) {
        Admin authAdmin = manager.getReference(Admin.class, admin.getId());
        String email;
        Review review;
        if(reviewDto.type().equals(SessionType.Organization)){
            OrganizationVerificationSession session = manager.getReference(OrganizationVerificationSession.class, reviewDto.sessionId());
            Organization organization = session.getOrganization();
            email = organizationService.getOrganizationEmail(organization);
            review = createOrgReview(session, reviewDto, authAdmin);
            switch (review.getStatus()){
                case Approved -> {
                    try{
                        organizationService.updateOrganizationDetails(session, organization);
                    }catch (DataIntegrityViolationException e){
                        throw new DuplicateAccountException(e.getLocalizedMessage());
                    }
                    emailService.sendVerificationStatusUpdate(organization.getContactFirstname(), email, Approved, review.getReview());
                }
                case Rejected -> {
                    organization.setStatus(VerificationStatus.UNVERIFIED);
                    emailService.sendVerificationStatusUpdate(organization.getContactFirstname(), email, ReviewStatus.Rejected, review.getReview());
                }
            }
            // Common attributes
        }else{
            VolunteerVerificationSession session = manager.getReference(VolunteerVerificationSession.class, reviewDto.sessionId());
            review = createVolunteerReview(session, reviewDto, authAdmin);
        }


        return mapper.toDto(reviewRepo.save(review));
    }

    private Review createVolunteerReview(VolunteerVerificationSession session, ReviewDto reviewDto, Admin admin){
        Review review = new Review(reviewDto.reason(), reviewDto.isApproved(), admin, session);
        ReviewStatus status = reviewDto.isApproved()? Approved: ReviewStatus.Rejected;

        session.setReview(reviewDto.reason());
        session.setReviewStatus(status);

        return review;
    }

    private Review createOrgReview(OrganizationVerificationSession session, ReviewDto reviewDto, Admin admin){
        Review review = new Review(reviewDto.reason(), reviewDto.isApproved(), admin, session);
        ReviewStatus status = reviewDto.isApproved()? Approved: ReviewStatus.Rejected;

        session.setReview(reviewDto.reason());
        session.setReviewStatus(status);

        return review;
    }

    public @Nullable ReviewResponseDto getLastSessionReview(Long sessionId) {
        OrganizationVerificationSession session = manager.getReference(OrganizationVerificationSession.class, sessionId);
        Review review = reviewRepo.findFirstByVerificationSessionOrderByCreatedAt(session).orElseThrow();

        return  mapper.toDto(review);
    }

    public ReviewResponseDto getLastVolunteerSessionReview(Long sessionId){
        VolunteerVerificationSession session = manager.getReference(VolunteerVerificationSession.class, sessionId);
        Review review = reviewRepo.findFirstByVolunteerVerificationSessionOrderByCreatedAt(session).orElseThrow();

        return mapper.toDto(review);
    }

    public void retryAutomaticVerification(Long sessionId){
        OrganizationVerificationSession session = verificationService.getVerificationSession(sessionId);
        verificationWorker.verifyContactPersonInformation(session);
    }
}
