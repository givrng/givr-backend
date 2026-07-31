package com.backend.givr.organization.entity;

import com.backend.givr.shared.Location;
import com.backend.givr.shared.Skill;
import com.backend.givr.shared.enums.ProjectStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.SQLRestriction;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long projectId;

    @ManyToOne
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @NotBlank
    @Column(nullable = false, unique = true)
    private String title;
    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String category;

    @Min(value = 1, message = "Cannot create a project for no volunteer")
    @Column(nullable = false)
    private Integer maxVolunteers;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "location", nullable = false)
    private Location location;

    @Column(nullable = false)
    private Date startDate;
    @Column(nullable = false)
    private Date endDate;
    @Column(nullable = false)
    private Date deadline;
    @Column(nullable = false)
    @NotBlank
    private String attendanceHours;

    @Enumerated(EnumType.STRING)
    private ProjectStatus status;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "project_skills", joinColumns = @JoinColumn(name = "project_id"), inverseJoinColumns = @JoinColumn(name = "skill_id"))
    private Set<Skill> requiredSkills;

    private String specialRequirements;

    @OneToMany(mappedBy = "project")
    private Set<ProjectApplication> applicationList;

    @OneToMany(mappedBy = "project")
    @SQLRestriction(value = "status = 'APPROVED'")
    private Set<ProjectApplication> approvedList;

    private ZonedDateTime createdAt;
    private ZonedDateTime modifiedAt;

    @PrePersist
    private void setCreatedAt(){
        this.createdAt = ZonedDateTime.now();
    }
    @PreUpdate
    private void setModifiedAt(){
        this.modifiedAt = ZonedDateTime.now();
    }
}
