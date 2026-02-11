package com.backend.givr.volunteer.service;

import com.backend.givr.organization.dtos.ProjectResponseDto;
import com.backend.givr.organization.entity.Project;
import com.backend.givr.organization.entity.ProjectApplication;
import com.backend.givr.organization.service.ApplicationService;
import com.backend.givr.organization.service.ParticipationService;
import com.backend.givr.organization.service.ProjectService;
import com.backend.givr.shared.Location;
import com.backend.givr.shared.Skill;
import com.backend.givr.shared.dtos.ParticipationDto;
import com.backend.givr.shared.dtos.PasswordUpdateDto;
import com.backend.givr.shared.dtos.ProjectApplicationForm;
import com.backend.givr.shared.email.EmailService;
import com.backend.givr.shared.enums.AccountType;
import com.backend.givr.shared.enums.OtpPurpose;
import com.backend.givr.shared.enums.ProjectStatus;
import com.backend.givr.shared.exceptions.IllegalOperationException;
import com.backend.givr.shared.interfaces.SecurityDetails;
import com.backend.givr.shared.mapper.ProjectMapper;
import com.backend.givr.shared.enums.AuthProviderType;
import com.backend.givr.shared.otp.OTPService;
import com.backend.givr.shared.repo.SkillRepo;
import com.backend.givr.shared.service.LocationService;
import com.backend.givr.shared.service.SkillService;
import com.backend.givr.shared.service.TokenIdService;
import com.backend.givr.volunteer.dtos.*;
import com.backend.givr.volunteer.entity.Volunteer;
import com.backend.givr.volunteer.mappings.VolunteerMapper;
import com.backend.givr.volunteer.repo.VolunteerRepo;
import com.backend.givr.volunteer.security.VolunteerDetails;
import com.backend.givr.volunteer.security.VolunteerDetailsService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class VolunteerService {
    private final Logger logger = LoggerFactory.getLogger(VolunteerService.class);
    @Autowired
    private VolunteerRepo repo;
    @Autowired
    private VolunteerDetailsService detailsService;
    @Autowired
    private VolunteerMapper mapper;

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private TokenIdService tokenService;

    @Autowired
    private SkillService skillService;
    @Autowired
    private LocationService locationService;
    @Autowired
    private ParticipationService participationService;
    @Autowired
    private EmailService emailService;
    @Autowired
    private OTPService otpService;

    @Autowired
    private SkillRepo skillRepo;
    @PersistenceContext
    private EntityManager manager;

    @Value("${client.app.baseUrl}")
    private String clientAppBaseUrl;

    public Volunteer getVolunteer(String id){
        return repo.findById(id).orElseThrow();
    }

    public Volunteer createVolunteer(CreateVolunteerRequestDto volunteerDto){
        if(!volunteerDto.validatePassword())
            throw new IllegalStateException("Password and confirm password do not match");

        Volunteer volunteer = mapper.toVolunteer(volunteerDto);
        Location location = locationService.createLocation(volunteer.getLocation());
        volunteer.getLocation().setId(location.getId());
        volunteer.setEmailIsVerified(false);
        volunteer.setProfileCompleted(true);
        volunteer.setPhoneIsVerified(false);
        return updateSkills(volunteer, volunteerDto.getInterests());
    }

    public VolunteerDashboard getVolunteerDashboard(String volunteerId){
        Volunteer volunteer = manager.getReference(Volunteer.class, volunteerId);
        List<ProjectApplication> applications = applicationService.getAppliedProjects(volunteer);
        return new VolunteerDashboard(volunteer.getFirstname(), volunteer.getProfileCompleted(), projectMapper.toApplicationsDto(applications));
    }

    public VolunteerProfile getVolunteerProfile(String volunteerId){
        return mapper.toProfile(getVolunteer(volunteerId));
    }
    public void createAuthPrincipal (CreateVolunteerRequestDto volunteerDto, Volunteer volunteer){
        VolunteerDetails details = new VolunteerDetails(volunteerDto.getEmail(), encoder.encode(volunteerDto.getPassword()), volunteer);
        detailsService.save(details);
    }


    public Volunteer createAccount(CreateVolunteerRequestDto volunteerDto){
        try{
            var volunteer = createVolunteer(volunteerDto);
            createAuthPrincipal(volunteerDto, volunteer);
            emailService.sendWelcomeEmail(volunteerDto.getFirstname(),String.format("%s/signin/volunteer", clientAppBaseUrl) , volunteerDto.getEmail());
            return volunteer;
        }catch (IllegalStateException e){
            logger.error("Failed to create account for user: {}", e.getLocalizedMessage());
            return null;
        }
    }

    public Volunteer updateSkills(Volunteer volunteer, List<String> skills){
        var updateSkills = skillService.updateSkills(skills);
        volunteer.setSkills(updateSkills);
       return repo.save(volunteer);
    }

    @Transactional
    public VolunteerProfile updateProfile(String volunteerId, UpdateVolunteerDto updatedVolunteerDto, SecurityDetails details){
        Volunteer volunteer = manager.getReference(Volunteer.class, volunteerId);
        Location location = locationService.createLocation(updatedVolunteerDto.getLocation());
        volunteer.setLocation(location);
        Set<Skill> skills = skillService.updateSkills(updatedVolunteerDto.getSkills());
        mapper.updateVolunteer(updatedVolunteerDto, volunteer);
        volunteer.setSkills(skills);
        volunteer.setProfileCompleted(true);
        if(updatedVolunteerDto.getEmail() != null && !updatedVolunteerDto.getEmail().equals(details.getUsername())){
            if(details.getProviderType() == AuthProviderType.LOCAL){
                VolunteerDetails volunteerDetails = detailsService.loadUserByUsername(details.getUsername());
                volunteerDetails.setEmail(updatedVolunteerDto.getEmail());
            }else{
                throw new IllegalOperationException("Social media login, cannot modify email");
            }
        }
        return mapper.toProfile(volunteer);
    }

    public void apply(SecurityDetails details, @Valid ProjectApplicationForm applicationForm) {
        Volunteer volunteer = manager.getReference(Volunteer.class, details.getId());
        applicationService.apply(volunteer, applicationForm, details.getUsername());
    }

    public List<ParticipationDto> getMyVolunteering(SecurityDetails details){
        Volunteer volunteer = manager.getReference(Volunteer.class, details.getId());
        return projectMapper.toParticipationDto(participationService.getVolunteerParticipation(volunteer));
    }

    @Async
    public void requestOtp(String email, OtpPurpose purpose) {
        Optional<VolunteerDetails> volunteer = detailsService.getDetails(email);
        if(volunteer.isPresent()){
            VolunteerDetails details = volunteer.get();
            if(details.getAuthProvider() != AuthProviderType.GOOGLE)
                emailService.sendOtpTo(email, AccountType.VOLUNTEER, purpose);
            else
                emailService.sendPasswordChangeNotificationForOauthUser(email);
        }
        else
            throw new IllegalOperationException("User does not have an account");
    }

    public void confirmEmail(SecurityDetails details, @Email String otp) {
        otpService.verifyOtp(details.getUsername(), otp, AccountType.VOLUNTEER, OtpPurpose.EMAIL_VERIFICATION);
        Volunteer volunteer = repo.findById(details.getId()).orElseThrow();
        volunteer.setEmailIsVerified(true);
        repo.save(volunteer);
    }

    @Transactional
    public void resetPassword(String email, String newPassword, String otp){
        otpService.verifyOtp(email, otp, AccountType.VOLUNTEER, OtpPurpose.PASSWORD_UPDATE);
        detailsService.updatePassword(encoder.encode(newPassword), email );
    }

    public List<ProjectResponseDto> getRecommendedProjects(SecurityDetails details) {
        Volunteer volunteer = manager.getReference(Volunteer.class, details.getId());
        if(volunteer.getLocation() == null || volunteer.getSkills() == null)
            return Collections.emptyList();

        var projects = projectService.getVolunteerRecommendedProjects(volunteer, ProjectStatus.OPEN).stream().filter(project -> {
                    LocalDateTime endOfDay = project.getDeadline().atTime(23, 59, 59);
                    return endOfDay.isAfter(LocalDateTime.now());
                })
                .sorted(Comparator.comparing(Project::getCreatedAt))
                .toList();;

        return projectMapper.toDtos(projects);
    }

    public void rejectParticipation(Long participationId, String id) {
        Volunteer volunteer = manager.getReference(Volunteer.class, id);
        participationService.deleteVolunteerParticipation(participationId, volunteer );
    }

    public void updatePassword(SecurityDetails details, PasswordUpdateDto passwordUpdateDto) {
        VolunteerDetails volunteerDetails = detailsService.loadUserByUsername(details.getUsername());
        otpService.verifyOtp(details.getUsername(), passwordUpdateDto.otp(), AccountType.VOLUNTEER, OtpPurpose.PASSWORD_UPDATE);
        volunteerDetails.setPassword(encoder.encode(passwordUpdateDto.password()));
    }
}
