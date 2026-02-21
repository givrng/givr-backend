package com.backend.givr.organization.oauth2;

import com.backend.givr.organization.entity.Organization;
import com.backend.givr.organization.repo.OrganizationRepo;
import com.backend.givr.organization.security.OrganizationDetails;
import com.backend.givr.organization.security.OrganizationDetailsRepo;
import com.backend.givr.shared.email.EmailService;
import com.backend.givr.shared.enums.VerificationStatus;
import com.backend.givr.shared.exceptions.DuplicateAccountException;
import com.backend.givr.shared.enums.AuthProviderType;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class OrganizationOAuthService implements OAuth2UserService<OidcUserRequest, OidcUser> {
    @Autowired
    private OrganizationRepo repo;
    @Autowired
    private OrganizationDetailsRepo detailsRepo;

    private final OidcUserService delegate = new OidcUserService();

    @Autowired
    private EmailService emailService;

    @Value("${client.app.baseUrl}")
    private String clientBaseUrl;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser user = delegate.loadUser(userRequest);
        Optional<OrganizationDetails> details = detailsRepo.findByEmail(user.getEmail());

        if(details.isEmpty()){
            createOrganization(user);
        }
        return user;
    }

    @Transactional
    private void createOrganization(OidcUser user){
        Organization organization = new Organization();
        organization.setProfileCompleted(false);
        organization.setStatus(VerificationStatus.UNVERIFIED);
        organization.setEmailVerified(user.getEmailVerified());
        organization.setContactFirstname(user.getGivenName());
        organization.setContactLastname(user.getFamilyName());
        organization.setContactMiddleName(user.getMiddleName());
        organization.setProfileUrl(user.getPicture());

        try{
            var savedOrganization = repo.save(organization);
            OrganizationDetails details = new OrganizationDetails( user.getSubject(), user.getEmail(), AuthProviderType.GOOGLE, savedOrganization);
            detailsRepo.save(details);
            emailService.sendOrganizationWelcomeEmail(organization.getContactFirstname(), String.format("%s/signin/organization", clientBaseUrl), user.getEmail());
        }catch (DataIntegrityViolationException | ConstraintViolationException ignored){
            throw new DuplicateAccountException("Organization violates constraints");
        }
    }
}
