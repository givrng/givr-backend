package com.backend.givr.shared.entity;

import com.backend.givr.organization.entity.Organization;
import com.backend.givr.shared.entity.projects.Project;
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
@Inheritance(strategy = InheritanceType.JOINED)
public class ProjectApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @ManyToOne
    @JoinColumn(nullable = false, name = "volunteer_id")
    protected Volunteer volunteer;

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false, name = "project_id")
    protected Project project;

    @ManyToOne
    @JoinColumn(nullable = true, name = "organization")
    protected Organization organization;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    protected ApplicationStatus status;

    protected String email;
    protected ZonedDateTime appliedAt;
    protected ZonedDateTime updatedAt;

    @Column(length = 500, nullable = false)
    protected String applicationReason;

    @Deprecated
    protected String availableDays;

    protected Boolean isAvailable;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "project_applicant_skills",
            joinColumns = @JoinColumn(name = "project_applicant_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id", referencedColumnName = "id")
    )
    protected List<Skill> specialSkills;
    @Column(length = 300)
    protected String aboutVolunteer;
    @Column(length = 200)
    protected String additionalInfo;

    public ProjectApplication(Project project, Volunteer volunteer, String email){
        this.project = project;
        this.volunteer = volunteer;
        this.status = ApplicationStatus.APPLIED;
        this.email = email;
    }

    @PrePersist
    protected void setAppliedAt(){
        this.appliedAt = ZonedDateTime.now();
    }

    @PreUpdate
    protected void setUpdatedAt(){
        this.updatedAt = ZonedDateTime.now();
    }
}
