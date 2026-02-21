package com.backend.givr.organization.entity;

import com.backend.givr.shared.enums.ParticipationStatus;
import com.backend.givr.volunteer.entity.Volunteer;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@NoArgsConstructor
@Table(uniqueConstraints = @UniqueConstraint(name = "volunteer_project_unq", columnNames = {"project_id", "volunteer_id"}))
@Getter
public class Participation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Setter
    private ParticipationStatus participationStatus;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id")
    @Setter
    private ProjectApplication projectApplication;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    @Setter
    private Organization organization;
    @ManyToOne(optional = false)
    @Setter
    @JoinColumn(name = "volunteer_id")
    private Volunteer volunteer;

    @Transient
    private Boolean reviewable;


    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    private void setCreatedAt(){
        this.createdAt = LocalDateTime.now(ZoneId.of("Africa/Lagos"));
    }
    @PreUpdate
    private  void setUpdatedAt(){
        this.updatedAt = LocalDateTime.now(ZoneId.of("Africa/Lagos"));
    }
    @PostLoad
    private void setReviewable(){
        this.reviewable = project.getReviewable();
    }
}
