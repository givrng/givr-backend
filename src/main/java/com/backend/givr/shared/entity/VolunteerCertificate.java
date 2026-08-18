package com.backend.givr.shared.entity;

import com.backend.givr.organization.entity.Participation;
import com.backend.givr.organization.entity.Project;
import com.backend.givr.volunteer.entity.Volunteer;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.UUID;


@Entity
@Getter
public class VolunteerCertificate {
    @Id
    private final String certId;
    @Setter
    private String certUrl;
    @ManyToOne
    @JoinColumn(name = "project_id")
    private final Project project;
    @ManyToOne
    @JoinColumn(name = "volunteer_id")
    private final Volunteer certifiedVolunteer;

    private ZonedDateTime issuedAt;

    @PrePersist
    private void setIssuedAt(){
        issuedAt = ZonedDateTime.now();
    }

    public VolunteerCertificate(Participation participant){
        this.project = participant.getProject();
        this.certifiedVolunteer = participant.getVolunteer();
        this.certId = String.format("Givr-cert-%S", UUID.randomUUID().toString().substring(0, 7));
    }
}
