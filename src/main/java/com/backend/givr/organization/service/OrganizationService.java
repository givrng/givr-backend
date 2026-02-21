package com.backend.givr.organization.service;

import com.backend.givr.organization.dtos.*;
import com.backend.givr.organization.entity.Organization;
import com.backend.givr.shared.dtos.ParticipationDto;
import com.backend.givr.organization.entity.Project;
import com.backend.givr.organization.mappings.OrganizationMapper;
import com.backend.givr.organization.repo.OrganizationRepo;
import com.backend.givr.organization.security.OrganizationDetails;
import com.backend.givr.organization.security.OrganizationDetailsService;
import com.backend.givr.shared.dtos.PasswordUpdateDto;
import com.backend.givr.shared.dtos.VolunteerApplicationDto;
import com.backend.givr.shared.notification.EmailService;
import com.backend.givr.shared.entity.OrganizationVerificationSession;
import com.backend.givr.shared.enums.*;
import com.backend.givr.shared.exceptions.DuplicateAccountException;
import com.backend.givr.shared.exceptions.IllegalOperationException;
import com.backend.givr.shared.interfaces.SecurityDetails;
import com.backend.givr.shared.mapper.ProjectMapper;
import com.backend.givr.shared.enums.AuthProviderType;
import com.backend.givr.shared.otp.OTPService;
import com.backend.givr.shared.service.LocationService;
import com.backend.givr.shared.service.SkillService;
import com.backend.givr.shared.service.VerificationService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class OrganizationService {
    @Autowired
    private OrganizationMapper mapper;
    @Autowired
    private ProjectMapper projectMapper;
    @Autowired
    private OrganizationRepo repo;
    @Autowired
    private OrganizationDetailsService service;
    @Autowired
    private SkillService skillService;
    @Autowired
    private ApplicationService applicationService;
    @Autowired
    private ParticipationService participationService;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private PasswordEncoder encoder;
    @Autowired
    private LocationService locationService;
    @Autowired
    private EmailService emailService;
    @Autowired
    private VerificationService verificationService;
    @Autowired
    private OTPService otpService;
    @Autowired
    private EntityManager em;

    @Value("${client.app.baseurl}")
    private String clientAppBaseUrl;
    @Transactional
    public Organization createOrganization(CreateOrganizationDto organizationDto){
        if(organizationDto == null){
            throw new IllegalArgumentException("Organization DTO cannot be null");
        }
        Organization organization = mapper.toOrganization(organizationDto);

        organization.setProfileCompleted(orgProfileComplete(organization));
        organization.setLocation(locationService.createLocation(organization.getLocation()));
        organization.setStatus(VerificationStatus.UNVERIFIED);
        organization.setEmailVerified(false);

        try{
            var savedOrganization = repo.save(organization);
            OrganizationDetails details = new OrganizationDetails(organizationDto.getEmail(), encoder.encode(organizationDto.getPassword()), savedOrganization);
            service.save(details);
            emailService.sendOrganizationWelcomeEmail(savedOrganization.getContactFirstname(), String.format("%s/organization", clientAppBaseUrl), details.getUsername() );
            return savedOrganization;
        }catch (DataIntegrityViolationException | ConstraintViolationException ignored){
            throw new DuplicateAccountException("An organization with the same cac registration number or email already exists ");
        }
    }

    private boolean orgProfileComplete(Organization organization){
        return organization.getOrganizationName() != null && organization.getOrganizationType() != null && organization.getCacRegNumber() != null;
    }

    @Transactional
    public List<ProjectResponseDto> createProject(ProjectRequestDto projectRequestDto, SecurityDetails details){
        if(projectRequestDto == null)
            throw new IllegalArgumentException("Null project DTO null accepted");

        var organization = repo.findById(details.getId());
        if(organization.isEmpty())
            throw new EntityNotFoundException("Failed to fetch organization");

        Organization org = organization.get();

        if(!org.getProfileCompleted())
            throw new IllegalOperationException("Profile not complete, cannot create project");

        projectService.createProject(projectRequestDto, org);

        return projectMapper.toDtos(projectService.getProjectByOrganizationAndStatus(org, ProjectStatus.DRAFT));
    }

    public void approveApplication(Long applicationId){
        applicationService.changeApplicationStatus(applicationId, ApplicationStatus.APPROVED);
    }

    public void rejectApplication(Long applicationId){
        applicationService.changeApplicationStatus(applicationId, ApplicationStatus.REJECTED);
    }

    public List<ParticipationDto> getProjectParticipants(SecurityDetails details){
        Organization organization = repo.findById(details.getId()).orElseThrow();
        return projectMapper.toParticipationDto(participationService.getParticipantsByOrganization(organization));
    }
    public void updateVolunteerParticipation(UpdateParticipantDto payload){
        participationService.changeParticipationStatus(payload.id(), payload.status());
    }
    public List<VolunteerApplicationDto> getProjectApplications (SecurityDetails details){
        Organization organization = repo.findById(details.getId()).orElseThrow();
        return applicationService.getProjectsApplications(organization);
    }

    public void publishProject(Long projectId){
        Project project = projectService.findProjectById(projectId);
        project.setStatus(ProjectStatus.OPEN);
        projectService.save(project);
    }

    public OrganizationDashboard getOrganizationDashboard(SecurityDetails details){
        var organization = repo.findById(details.getId()).orElseThrow(()-> new EntityNotFoundException(String.format("Organization withID %s does not exist", details.getId())));

        Map<String, List<ProjectResponseDto>> projectDtoMap = new HashMap<>();

        projectDtoMap.put("draftProjects", projectMapper.toDtos(
                organization.getProjects()
                        .stream()
                        .filter(project -> project.getStatus() == ProjectStatus.DRAFT)
                        .toList()
        ));

        projectDtoMap.put("openProjects", projectMapper.toDtos(
                organization.getProjects()
                        .stream()
                        .filter(project -> project.getStatus() == ProjectStatus.OPEN)
                        .sorted(Comparator.comparing(Project::getCreatedAt))
                        .toList()
        ));


        projectDtoMap.put("ongoingProjects", projectMapper.toDtos(
                organization.getProjects()
                        .stream()
                        .filter(project -> project.getStatus() == ProjectStatus.ONGOING)
                        .sorted(Comparator.comparing(Project::getCreatedAt))
                        .toList()
        ));

        projectDtoMap.put("completedProjects", projectMapper.toDtos(organization.getProjects()
                .stream()
                .filter(project -> project.getStatus() == ProjectStatus.COMPLETED)
                .sorted(Comparator.comparing(Project::getCreatedAt))
                .toList()));

        ApplicationStats stats = applicationService.getVolunteerStats(organization);

        return new OrganizationDashboard(organization.getOrganizationName(), projectDtoMap, 5.0, stats ,!(organization.getStatus() == VerificationStatus.VERIFIED));
    }

    public List<Project> getProjects(SecurityDetails details){
        return projectService.getOrganizationProjects(em.getReference(Organization.class, details.getId()));
    }

    @Transactional
    public ProjectResponseDto updateProject(Long projectId, ProjectRequestDto projectRequestDto) {
        return projectMapper.toProjectDto(projectService.updateProject(projectId, projectRequestDto));
    }
    public List<OrganizationDto> getOrganizations() {
        return mapper.toOrganizationDtoList(repo.findAll());
    }

    public void deleteProject(Long projectId, String organizationId) {
        Organization organization = em.getReference(Organization.class, organizationId);
        projectService.deleteProject(projectId, organization);
    }

    @Async
    public void requestOtp( String email, OtpPurpose purpose) {
        Optional<OrganizationDetails> details = service.getDetails(email);
        if(details.isPresent()) {
            OrganizationDetails organizationDetails = details.get();
            if(organizationDetails.getAuthProvider()!=AuthProviderType.GOOGLE)
                emailService.sendOtpTo(email, AccountType.ORGANIZATION, purpose);
            else
                emailService.sendPasswordChangeNotificationForOauthUser(email);
        }
        else
            throw new IllegalOperationException("Account does not exist");
    }

    @Transactional
    public void confirmEmail(SecurityDetails details, String Otp){
        otpService.verifyOtp(details.getUsername(), Otp, AccountType.ORGANIZATION, OtpPurpose.EMAIL_VERIFICATION);

        Organization organization = repo.findById(details.getId()).orElseThrow(()->new EntityNotFoundException("User with email does not exist"));
        organization.setEmailVerified(true);
        repo.save(organization);
    }

    public void resetPassword(String email, String newPassword, String otp) {
        otpService.verifyOtp(email, otp, AccountType.ORGANIZATION, OtpPurpose.PASSWORD_UPDATE);
        service.updatePassword(encoder.encode(newPassword), email );
    }

    public OrganizationProfileDto getOrganizationProfile(SecurityDetails details) {
        Organization organization = em.getReference(Organization.class, details.getId());
        return toProfile(organization, details);
    }

    @Transactional
    public OrganizationProfileDto updateOrganization(OrganizationUpdateDto organizationDto, SecurityDetails details) {
        Organization organization = em.getReference(Organization.class, details.getId());

        if((organizationDto.getEmail()!= null) && !Objects.equals(organizationDto.getEmail(), details.getUsername())) {
            organization.setEmailVerified(false);
            service.updateEmail(organizationDto.getEmail(), details.getUsername());
        }

        mapper.updateOrganization(organizationDto, organization);
        boolean createdVerificationSession = verificationService.createVerificationSession(organization, organizationDto);

        if(createdVerificationSession)
            emailService.sendVerificationStatusUpdate(organization.getContactFirstname(), details.getUsername(), ReviewStatus.Pending, null);
        return toProfile(organization, details);
    }

    public void updateOrganizationDetails (OrganizationVerificationSession session, Organization organization){
        organization.setStatus(VerificationStatus.VERIFIED);
        organization.setProfileCompleted(true);
        organization.setOrganizationType(session.getClaimedType());
        organization.setOrganizationName(session.getClaimedOrgName());
        organization.setCacRegNumber(session.getClaimedCACRegNumber());
        organization.setLocation(session.getClaimedLocation());
        organization.setAddress(session.getClaimedAddress().address());
    }

    public String getOrganizationEmail(Organization organization){
        return service.getEmail(organization);
    }

    private OrganizationProfileDto toProfile(Organization organization, SecurityDetails details){
        OrganizationDto orgDto = mapper.toOrganizationDto(organization);

        OrganizationContactDto orgContact = mapper.toOrganizationContact(organization);
        orgContact.setEmail(details.getUsername());
        orgContact.setEmailEditable(details.getProviderType() == AuthProviderType.LOCAL);
        return new OrganizationProfileDto(orgContact, orgDto);
    }

    @Transactional
    public void updatePassword(@Valid PasswordUpdateDto passwordUpdateDto, SecurityDetails details) {
        OrganizationDetails orgDetails = service.loadUserByUsername(details.getUsername());
        otpService.verifyOtp(details.getUsername(), passwordUpdateDto.otp(), AccountType.ORGANIZATION, OtpPurpose.PASSWORD_UPDATE);
        orgDetails.setPassword(encoder.encode(passwordUpdateDto.password()));
    }

    public EmailExists emailExists(String email, SecurityDetails details) {
        return new EmailExists(email, !Objects.equals(email, details.getUsername()) && service.emailExist(email));
    }
}
