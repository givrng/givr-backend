package com.backend.givr.volunteer.entity;

import com.backend.givr.shared.entity.Location;
import com.backend.givr.shared.entity.Skill;
import com.backend.givr.shared.enums.AuthProviderType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;


@Entity
@Getter
@Setter
public class Volunteer {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String volunteerId;
    private String firstname;
    private String middleName;
    private String lastname;
    private String phoneNumber;

    // Security information
    @Email(message = "Invalid email format")
    @Column(unique = true, nullable = false)
    private String email;
    private String password;
    @Enumerated(EnumType.STRING)
    private AuthProviderType authProvider;
    private String providerId;
    //--- end ---
    @ManyToOne
    @JoinColumn(name = "location_id")
    private Location location;

    private Boolean emailIsVerified;
    private Boolean phoneIsVerified;
    @URL
    private String profileUrl;
    private double rating = 0.0;

    @ManyToMany(cascade = CascadeType.MERGE)
    @JoinTable(name = "volunteer_skills", joinColumns = @JoinColumn(name = "volunteer_id"), inverseJoinColumns = @JoinColumn(name = "skill_id"))
    private Set<Skill> skills = new HashSet<>();

    private Boolean profileCompleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public void setOAuthDetails(OidcUser user, AuthProviderType type){
        this.email = user.getEmail();
        this.authProvider = type;
        this.providerId = user.getSubject();
    }

    public void setUsernamePasswordDetails(String email, String password){
        this.email = email;
        this.password = password;
        this.authProvider = AuthProviderType.LOCAL;
    }
    @PrePersist
    private void setCreatedAt(){
        this.createdAt = LocalDateTime.now(ZoneId.of("Africa/Lagos"));
    }

    @PreUpdate
    private void setUpdatedAt() {
        this.updatedAt = LocalDateTime.now(ZoneId.of("Africa/Lagos"));
    }
}
