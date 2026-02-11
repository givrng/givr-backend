package com.backend.givr.organization.security;

import com.backend.givr.organization.entity.Organization;
import com.backend.givr.shared.interfaces.SecurityDetails;
import com.backend.givr.shared.enums.AuthProviderType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;

@Entity
@NoArgsConstructor
@Table(uniqueConstraints = @UniqueConstraint(name = "provider_providerId_unique", columnNames = {"provider_id", "auth_provider"}))
public class OrganizationDetails implements SecurityDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Email(message = "Invalid Email format")
    @Column(unique = true, nullable = false)
    private String email;

    @Setter
    private String password;
    private Collection<GrantedAuthority> roles = List.of(new SimpleGrantedAuthority("ORGANIZATION"));

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "organization_id")
    @Getter
    private Organization organization;

    @Getter
    @Enumerated(EnumType.STRING)
    private AuthProviderType authProvider;

    @Getter
    private String providerId;

    public OrganizationDetails(String email, String password, Organization organization){
        this.email = email;
        this.password = password;
        this.organization = organization;
        this.authProvider = AuthProviderType.LOCAL;
    }

    public OrganizationDetails(String providerId, String email, AuthProviderType provider, Organization organization){
        this.providerId = providerId;
        this.email= email;
        this.organization = organization;
        this.authProvider = provider;
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

    @Override
    public AuthProviderType getProviderType() {
        return this.authProvider;
    }
}
