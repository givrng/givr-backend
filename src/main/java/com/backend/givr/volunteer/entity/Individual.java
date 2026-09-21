package com.backend.givr.volunteer.entity;

import com.backend.givr.shared.enums.VerificationStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Individual {
    @Id
    private String individualId;

    private String email;

    private String firstname;
    private String lastname;
    private String middleName;
    private String logo;
    @Enumerated(EnumType.STRING)
    private VerificationStatus status;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    @PrePersist
    private void setCreatedAt(){
        this.createdAt = ZonedDateTime.now();
    }
    @PreUpdate
    private void setUpdatedAt(){
        this.updatedAt = ZonedDateTime.now();
    }

    public Individual(VolunteerVerificationSession verificationSession){
        this.individualId = verificationSession.getVolunteer().getVolunteerId();
        this.email = verificationSession.getVolunteer().getEmail();
        this.firstname = verificationSession.getFirstname();
        this.lastname = verificationSession.getLastname();
        this.middleName = verificationSession.getMiddleName();
        this.logo = verificationSession.getIndividualLogo();
        this.status = verificationSession.getVerificationStatus();
    }
}
