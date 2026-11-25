package com.backend.givr.admin.service;

import com.backend.givr.admin.dtos.*;
import com.backend.givr.admin.entity.Admin;
import com.backend.givr.admin.entity.AdminDetails;
import com.backend.givr.admin.entity.ProjectReview;
import com.backend.givr.admin.entity.Review;
import com.backend.givr.admin.enums.AdminRole;
import com.backend.givr.admin.enums.SessionType;
import com.backend.givr.admin.mapper.AdminMapper;
import com.backend.givr.admin.repos.AdminRepo;
import com.backend.givr.admin.repos.ProjectReviewRepo;
import com.backend.givr.admin.repos.ReviewRepo;
import com.backend.givr.organization.entity.Organization;
import com.backend.givr.organization.service.OrganizationService;
import com.backend.givr.shared.dtos.InitiativeResponseDto;
import com.backend.givr.shared.dtos.VerificationSessionDto;
import com.backend.givr.shared.dtos.VolunteerVerificationSessionDto;
import com.backend.givr.shared.email.EmailService;
import com.backend.givr.shared.entity.OrganizationVerificationSession;
import com.backend.givr.shared.entity.projects.Project;
import com.backend.givr.shared.enums.*;
import com.backend.givr.shared.exceptions.DuplicateAccountException;
import com.backend.givr.shared.interfaces.SecurityDetails;
import com.backend.givr.shared.mapper.ProjectMapper;
import com.backend.givr.shared.otp.OTPService;
import com.backend.givr.shared.service.InitiativeService;
import com.backend.givr.shared.service.VerificationService;
import com.backend.givr.shared.service.VerificationWorker;
import com.backend.givr.volunteer.entity.VolunteerVerificationSession;
import com.backend.givr.volunteer.service.VolunteerVerificationSessionService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PagedModel;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Objects;

import static com.backend.givr.shared.enums.ReviewStatus.Approved;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final AdminRepo repo;

    private final AdminDetailsService service;
    private final ReviewRepo reviewRepo;
    private final ProjectReviewRepo projectReviewRepo;

    private final OrganizationService organizationService;
    private final AdminMapper mapper;
    private final ProjectMapper projectMapper;
    private final EntityManager manager;

    private final EmailService emailService;
    private final OTPService otpService;
    private final VerificationService verificationService;
    private final VerificationWorker verificationWorker;
    private final InitiativeService initiativeService;
    private final VolunteerVerificationSessionService volunteerVerificationSessionService;

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

    @Transactional
    public ProjectReviewResponseDto createProjectReview(ProjectReviewDto reviewDto, AdminDetails details){
        Project project = initiativeService.getInitiative(reviewDto.projectId());
        ReviewStatus status = reviewDto.isApproved()? Approved: ReviewStatus.Rejected;
        Admin admin = manager.getReference(Admin.class, details.getId());

        if(reviewDto.isApproved()){
            project.setReviewStatus(status);
            emailService.sendProjectReviewStatusUpdate(project.getOrganizerName(),
                    project.getCreator().getIndividual().getEmail(), status, reviewDto.reason());
        }else {
            project.setReviewStatus(status);
            emailService.sendProjectReviewStatusUpdate(project.getOrganizerName(),
                    project.getCreator().getIndividual().getEmail(), status, reviewDto.reason());
        }

        return mapper.toDto(createProjectReview(reviewDto, project, admin));
    }

    private Review createVolunteerReview(VolunteerVerificationSession session, ReviewDto reviewDto, Admin admin){
        Review review = new Review(reviewDto.reason(), reviewDto.isApproved(), admin, session);
        session.setReview(reviewDto.reason());
        session.setReviewStatus(review.getStatus());
        return review;
    }

    private Review createOrgReview(OrganizationVerificationSession session, ReviewDto reviewDto, Admin admin){
        Review review = new Review(reviewDto.reason(), reviewDto.isApproved(), admin, session);
        ReviewStatus status = reviewDto.isApproved()? Approved: ReviewStatus.Rejected;

        session.setReview(reviewDto.reason());
        session.setReviewStatus(status);

        return review;
    }

    private ProjectReview createProjectReview(@Validated ProjectReviewDto reviewDto, Project project, Admin reviewer){
        ReviewStatus status = reviewDto.isApproved()? Approved: ReviewStatus.Rejected;
        return projectReviewRepo.save(new ProjectReview(
                reviewDto.reason(),
                status,
                project,
                reviewer
        ));
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

    public @Nullable PagedModel<InitiativeResponseDto> getInitiatives(ProjectStatus projectStatus, PageRequest pageRequest) {
        return initiativeService.getAllInitiativeByStatus(projectStatus, pageRequest);
    }
}
