package com.backend.givr.volunteer.security;

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

@NoArgsConstructor
public class VolunteerDetails implements SecurityDetails {
    @Setter
    private String email;

    @Setter
    private String password;
    private Collection<GrantedAuthority> roles = List.of(new SimpleGrantedAuthority("VOLUNTEER"));

    @Getter
    private Volunteer volunteer;
    @Getter

    private AuthProviderType authProvider;
    @Getter
    private String providerId;

    public VolunteerDetails(String email, String password, Volunteer volunteer){
        this.email = email;
        this.password = password;
        this.volunteer = volunteer;
        this.authProvider = AuthProviderType.LOCAL;
    }

    public VolunteerDetails(Volunteer volunteer){
        this.email = volunteer.getEmail();
        this.authProvider = volunteer.getAuthProvider();
        this.providerId = volunteer.getProviderId();
        this.password = volunteer.getPassword();
        this.volunteer = volunteer;
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
    public void setAuthorities() {
        this.roles = List.of(new SimpleGrantedAuthority("VOLUNTEER"));
    }

    @Override
    public AuthProviderType getProviderType() {
        return this.authProvider;
    }
}
