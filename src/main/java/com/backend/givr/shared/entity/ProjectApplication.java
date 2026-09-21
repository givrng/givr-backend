package com.backend.givr.shared.entity;

import com.backend.givr.organization.entity.Organization;
import com.backend.givr.shared.enums.ApplicationStatus;
import com.backend.givr.volunteer.entity.Individual;
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
    @JoinColumn(nullable = true, name = "organization")
    private Organization organization;

    @ManyToOne
    @JoinColumn(nullable = true, name = "individual")
    private Individual individual;

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

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "project_applicant_skills",
            joinColumns = @JoinColumn(name = "project_applicant_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id", referencedColumnName = "id")
    )
    private List<Skill> specialSkills;
    @Column(length = 300)
    private String aboutVolunteer;
    @Column(length = 200)
    private String additionalInfo;

    public ProjectApplication(Project project, Volunteer volunteer, String email){
        this.project = project;
        this.volunteer = volunteer;
        this.status = ApplicationStatus.APPLIED;
        if(project.getOrganization() != null){
            this.organization = project.getOrganization();
        }else{
            this.individual = project.getIndividual();
        }
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
