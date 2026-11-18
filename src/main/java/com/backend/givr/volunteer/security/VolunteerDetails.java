package com.backend.givr.volunteer.security;

import com.backend.givr.shared.enums.VerificationStatus;
import com.backend.givr.shared.interfaces.SecurityDetails;
import com.backend.givr.shared.enums.AuthProviderType;
import com.backend.givr.volunteer.entity.Volunteer;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.Collection;
import java.util.List;


public class VolunteerDetails implements SecurityDetails {
    @Setter
    private String email;

    @Setter
    private String password;
    private Collection<GrantedAuthority> roles;

    @Getter
    private final Volunteer volunteer;
    @Getter

    private final AuthProviderType authProvider;
    @Getter
    private String providerId;

    public VolunteerDetails(String email, String password, Volunteer volunteer){
        this.email = email;
        this.password = password;
        this.volunteer = volunteer;
        this.authProvider = AuthProviderType.LOCAL;
        setAuthorities(volunteer.getVerificationStatus());
    }

    public VolunteerDetails(Volunteer volunteer){
        this.email = volunteer.getEmail();
        this.authProvider = volunteer.getAuthProvider();
        this.providerId = volunteer.getProviderId();
        this.password = volunteer.getPassword();
        this.volunteer = volunteer;
        setAuthorities(volunteer.getVerificationStatus());
    }
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getId() {
        return volunteer.getVolunteerId();
    }

    @Override
    public void setAuthorities(VerificationStatus status) {
        this.roles = status.equals(VerificationStatus.VERIFIED)?List.of(new SimpleGrantedAuthority("VOLUNTEER"), new SimpleGrantedAuthority("INDIVIDUAL")) :
                List.of(new SimpleGrantedAuthority("VOLUNTEER"));
    }

    @Override
    public AuthProviderType getProviderType() {
        return this.authProvider;
    }

}
