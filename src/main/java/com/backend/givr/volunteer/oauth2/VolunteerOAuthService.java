package com.backend.givr.volunteer.oauth2;

import com.backend.givr.shared.email.EmailService;
import com.backend.givr.shared.exceptions.DuplicateAccountException;
import com.backend.givr.shared.enums.AuthProviderType;
import com.backend.givr.volunteer.entity.Volunteer;
import com.backend.givr.volunteer.repo.VolunteerRepo;
import com.backend.givr.volunteer.security.VolunteerDetails;
import com.backend.givr.volunteer.security.VolunteerDetailsRepo;
import jakarta.validation.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class VolunteerOAuthService implements OAuth2UserService<OidcUserRequest, OidcUser> {
    @Autowired
    private VolunteerDetailsRepo detailsRepo;
    @Autowired
    private VolunteerRepo volunteerRepo;
    @Autowired
    private EmailService emailService;

    @Value("${client.app.baseUrl}")
    private String clientBaseUrl;

    private final OidcUserService delegate = new OidcUserService();
    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser user = delegate.loadUser(userRequest);
        Optional<VolunteerDetails> volunteerDetails = detailsRepo.findByEmail(user.getEmail());

        if(volunteerDetails.isEmpty()){
            createVolunteer(user);
            emailService.sendWelcomeEmail(user.getGivenName(), String.format("%s/signin/volunteer", clientBaseUrl), user.getEmail());
        }
        return user;
    }

    private void createVolunteer(OidcUser user){
        Volunteer volunteer = new Volunteer();
        volunteer.setFirstname(user.getGivenName());
        volunteer.setMiddleName(user.getMiddleName());
        volunteer.setLastname(user.getFamilyName());
        volunteer.setEmailIsVerified(user.getEmailVerified());
        volunteer.setProfileUrl(user.getPicture());
        volunteer.setProfileCompleted(false);

        try{
            Volunteer savedVolunteer = volunteerRepo.save(volunteer);
            VolunteerDetails volunteerDetails = new VolunteerDetails(savedVolunteer, user, AuthProviderType.GOOGLE);
            detailsRepo.save(volunteerDetails);
        }catch (DataIntegrityViolationException | ConstraintViolationException ignored){
            throw new DuplicateAccountException("Volunteer account creation violates constraints");
        }
    }
}
