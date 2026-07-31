package com.backend.givr.volunteer.security;

import com.backend.givr.shared.interfaces.SecurityDetails;
import com.backend.givr.volunteer.entity.Volunteer;
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
public class VolunteerDetails implements SecurityDetails {
    @Id
    @Email(message = "Invalid email format")

    private String email;
    private String password;
    private Collection<GrantedAuthority> roles = List.of(new SimpleGrantedAuthority("VOLUNTEER"));

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "volunteer_id")
    @Getter
    private Volunteer volunteer;

    public VolunteerDetails(String email, String password, Volunteer volunteer){
        this.email = email;
        this.password = password;
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
}
