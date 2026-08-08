package com.backend.givr.volunteer.service;

import com.backend.givr.organization.dtos.ProjectResponseDto;
import com.backend.givr.organization.entity.Project;
import com.backend.givr.organization.entity.ProjectApplication;
import com.backend.givr.organization.service.ApplicationService;
import com.backend.givr.organization.service.ParticipationService;
import com.backend.givr.organization.service.ProjectService;
import com.backend.givr.shared.dtos.VolunteerCertificateDto;
import com.backend.givr.shared.entity.Location;
import com.backend.givr.shared.entity.Skill;
import com.backend.givr.shared.dtos.ParticipationDto;
import com.backend.givr.shared.dtos.PasswordUpdateDto;
import com.backend.givr.shared.dtos.ProjectApplicationForm;
import com.backend.givr.shared.email.EmailService;
import com.backend.givr.shared.enums.AccountType;
import com.backend.givr.shared.enums.OtpPurpose;
import com.backend.givr.shared.enums.ProjectStatus;
import com.backend.givr.shared.exceptions.DuplicateAccountException;
import com.backend.givr.shared.exceptions.IllegalOperationException;
import com.backend.givr.shared.interfaces.SecurityDetails;
import com.backend.givr.shared.mapper.CertificateMapper;
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
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutionException;

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
    private RedisTemplate<String, Object> redisTemplate;
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
    private CertificateMapper certMapper;

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
        volunteer.setUsernamePasswordDetails(volunteerDto.getEmail(), encoder.encode(volunteerDto.getPassword()));
        return updateSkills(volunteer, volunteerDto.getInterests());
    }

    public VolunteerDashboard getVolunteerDashboard(String volunteerId){
        Volunteer volunteer = manager.getReference(Volunteer.class, volunteerId);
        List<ProjectApplication> applications = applicationService.getAppliedProjects(volunteer);
        return new VolunteerDashboard(volunteer.getFirstname(), volunteer.getProfileCompleted(), projectMapper.toApplicationsDto(applications));
    }

    @Cacheable(cacheNames = "volunteerProfile", key = "#volunteerId")
    public VolunteerProfile getVolunteerProfile(String volunteerId){
        Volunteer volunteer = getVolunteer(volunteerId);
        List<VolunteerCertificateDto> certs = certMapper.toDto(volunteer.getCertificates());
        var profile =  mapper.toProfile(volunteer);
        profile.setCertificates(certs);
        return profile;
    }


    public Volunteer createAccount(CreateVolunteerRequestDto volunteerDto){
        try{
            var volunteer = createVolunteer(volunteerDto);
            emailService.sendWelcomeEmail(volunteerDto.getFirstname(),String.format("%s/signin/volunteer", clientAppBaseUrl) , volunteerDto.getEmail());
            return volunteer;
        }catch (IllegalStateException | DataIntegrityViolationException e){

            logger.error("Failed to create account for user: {}", e.getLocalizedMessage());
            throw new DuplicateAccountException("A account exist with this email");
        }
    }

    public Volunteer updateSkills(Volunteer volunteer, List<String> skills){
        var updateSkills = skillService.updateSkills(skills);
        volunteer.setSkills(updateSkills);
       return repo.save(volunteer);
    }

    public String getVolunteerEmail(String volunteerId){
        return Mono.just(Objects.requireNonNull(redisTemplate.opsForValue()
                        .get("volunteer:"+volunteerId)))
                .cast(String.class)
                .switchIfEmpty(
                        Mono.just(repo.findById(volunteerId).orElseThrow())
                                .map(Volunteer::getEmail)
                                .doOnNext(organization -> redisTemplate.opsForValue().set("volunteer:"+volunteerId, organization, Duration.ofHours(6)))
                ).block();
    }

    @Transactional
    @CachePut(cacheNames = "volunteerProfile", key = "#volunteerId")
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
                VolunteerDetails volunteerDetails = new VolunteerDetails(repo.findByEmail(details.getUsername()).orElseThrow());
                volunteer.setEmail(updatedVolunteerDto.getEmail());
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
        Optional<Volunteer> volunteer = detailsService.getDetails(email);
        if(volunteer.isPresent()){
            Volunteer details = volunteer.get();
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
        try{
            otpService.verifyOtp(details.getUsername(), passwordUpdateDto.otp(), AccountType.VOLUNTEER, OtpPurpose.PASSWORD_UPDATE);
            detailsService.updatePassword(encoder.encode(passwordUpdateDto.password()), details.getUsername());
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }

    }

    public String shareProject(Long projectId){
        try{
            return projectService.shareProject(projectId);
        } catch (ExecutionException | InterruptedException e) {
            System.err.printf("Failed to create shareable link, %s", e.getLocalizedMessage());
            throw new RuntimeException(e);
        }
    }

    public List<Volunteer> getAllByLocation(Location location){
        return repo.findAllByLocationState(location.getState());
    }
}
