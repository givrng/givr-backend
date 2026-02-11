package com.backend.givr.organization.security;

import com.backend.givr.organization.entity.Organization;
import com.backend.givr.shared.interfaces.SecurityDetails;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@NoArgsConstructor
public class OrganizationDetails implements SecurityDetails {
    @Id
    @Email(message = "Invalid Email format")
    private String email;
    private String password;
    private Collection<GrantedAuthority> roles = List.of(new SimpleGrantedAuthority("ORGANIZATION"));

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "organization_id")
    @Getter
    private Organization organization;

    public OrganizationDetails(String email, String password, Organization organization){
        this.email = email;
        this.password = password;
        this.organization = organization;
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
        return organization.getOrganizationId();
    }

    @Override
    public void setAuthorities() {
        this.roles = List.of(new SimpleGrantedAuthority("ORGANIZATION"));
    }
}
