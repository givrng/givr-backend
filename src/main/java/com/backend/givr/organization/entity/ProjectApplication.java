package com.backend.givr.organization.entity;

import com.backend.givr.shared.enums.ApplicationStatus;
import com.backend.givr.volunteer.entity.Volunteer;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;

@Entity
@Table(uniqueConstraints = @UniqueConstraint( columnNames = {"project", "volunteer"}), name = "AppliedProjects")
@Getter
@Setter
@NoArgsConstructor
public class ProjectApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(nullable = false, name = "volunteer_id")
    private Volunteer volunteer;

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false, name = "project_id")
    private Project project;

    @ManyToOne
    @JoinColumn(nullable = false, name = "organization")
    private Organization organization;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ApplicationStatus status;

    private ZonedDateTime appliedAt;
    private ZonedDateTime updatedAt;

    private String applicationReason;
    private String availableDays;

    public ProjectApplication(Project project, Volunteer volunteer){
        this.project = project;
        this.volunteer = volunteer;
        this.status = ApplicationStatus.APPLIED;
        this.organization = project.getOrganization();
    }

    @PrePersist
    private void setAppliedAt(){
        this.appliedAt = ZonedDateTime.now();
    }

    @PreUpdate
    private void setUpdatedAt(){
        this.updatedAt = ZonedDateTime.now();
    }
}
