package com.backend.givr.organization.entity;

import com.backend.givr.shared.entity.Skill;
import com.backend.givr.shared.enums.ApplicationStatus;
import com.backend.givr.volunteer.entity.Volunteer;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.List;

@Entity
@Table(uniqueConstraints = @UniqueConstraint( columnNames = {"project_id", "volunteer_id"}), name = "AppliedProjects")
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

    private String email;
    private ZonedDateTime appliedAt;
    private ZonedDateTime updatedAt;

    @Column(length = 500, nullable = false)
    private String applicationReason;

    @Deprecated
    private String availableDays;

    private Boolean isAvailable;

    @ManyToMany
    @JoinTable(
            name = "project_applicant_skills",
            joinColumns = @JoinColumn(name = "project_applicant_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id", referencedColumnName = "id"))
    private List<Skill> specialSkills;
    @Column(length = 300)
    private String aboutVolunteer;
    @Column(length = 200)
    private String additionalInfo;

    public ProjectApplication(Project project, Volunteer volunteer, String email){
        this.project = project;
        this.volunteer = volunteer;
        this.status = ApplicationStatus.APPLIED;
        this.organization = project.getOrganization();
        this.email = email;
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
