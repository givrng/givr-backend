package com.backend.givr.admin.service;

import com.backend.givr.admin.dtos.AdminAuthDto;
import com.backend.givr.admin.dtos.ReviewDto;
import com.backend.givr.admin.dtos.ReviewResponseDto;
import com.backend.givr.admin.entity.Admin;
import com.backend.givr.admin.entity.AdminDetails;
import com.backend.givr.admin.entity.Review;
import com.backend.givr.admin.enums.AdminRole;
import com.backend.givr.admin.mapper.AdminMapper;
import com.backend.givr.admin.repos.AdminRepo;
import com.backend.givr.admin.repos.ReviewRepo;
import com.backend.givr.organization.entity.Organization;
import com.backend.givr.organization.service.OrganizationService;
import com.backend.givr.shared.dtos.VerificationSessionDto;
import com.backend.givr.shared.email.EmailService;
import com.backend.givr.shared.entity.OrganizationVerificationSession;
import com.backend.givr.shared.enums.AccountType;
import com.backend.givr.shared.enums.OtpPurpose;
import com.backend.givr.shared.interfaces.SecurityDetails;
import com.backend.givr.shared.otp.OTPService;
import com.backend.givr.shared.service.VerificationService;
import com.backend.givr.shared.enums.ReviewStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.constraints.Email;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

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

    public @Nullable List<VerificationSessionDto> getVerificationSession(ReviewStatus status) {
        return Objects.requireNonNull(verificationService.getVerificationSessions(status)).stream().map(VerificationSessionDto::new).toList();
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
        OrganizationVerificationSession session = manager.getReference(OrganizationVerificationSession.class, reviewDto.sessionId());
        Review review = new Review(reviewDto.reason(), reviewDto.isApproved(), authAdmin, session);
        ReviewStatus status = reviewDto.isApproved()? ReviewStatus.Approved: ReviewStatus.Rejected;

        session.setReview(reviewDto.reason());
        session.setReviewStatus(status);
        // Common attributes
        Organization organization = session.getOrganization();
        String email = organizationService.getOrganizationEmail(organization);

        switch (status){
            case Approved -> {
                organizationService.updateOrganizationDetails(session, organization);
                emailService.sendVerificationStatusUpdate(organization.getContactFirstname(), email, ReviewStatus.Approved, review.getReview());
            }
            case Rejected -> {
                emailService.sendVerificationStatusUpdate(organization.getContactFirstname(), email, ReviewStatus.Rejected, review.getReview());
            }
        }
        return mapper.toDto(reviewRepo.save(review));
    }

    public @Nullable ReviewResponseDto getLastSessionReview(Long sessionId) {
        OrganizationVerificationSession session = manager.getReference(OrganizationVerificationSession.class, sessionId);
        Review review = reviewRepo.findFirstByVerificationSessionOrderByCreatedAt(session).orElseThrow();

        return  mapper.toDto(review);
    }
}
