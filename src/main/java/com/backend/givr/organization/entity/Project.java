package com.backend.givr.organization.entity;

import com.backend.givr.shared.entity.Location;
import com.backend.givr.shared.entity.Skill;
import com.backend.givr.shared.enums.ProjectStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.SQLRestriction;

import java.time.*;
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

    @Column(nullable = false, unique = true)
    private String title;

    @Column(nullable = false, length = 1000)
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
    private LocalDate startDate;
    @Column(nullable = false)
    private LocalDate endDate;
    @Column(nullable = false)
    private LocalDate deadline;

    @Transient
    private Boolean reviewable;
    @Embedded
    private AttendanceHours attendanceHours;

    private Boolean orgNotifiedOfDeadline;

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
        this.createdAt = ZonedDateTime.now(ZoneOffset.UTC);
    }
    @PreUpdate
    private void setModifiedAt(){
        this.modifiedAt = ZonedDateTime.now(ZoneOffset.UTC);
    }

    @Transient
    public int getVolunteerCount(){
        return approvedList==null? 0 : approvedList.size();
    }

    public boolean shouldClose(LocalDateTime now){
        return status != ProjectStatus.CLOSE && now.isAfter(deadline.atTime(23, 59, 59));
    }

    public void closeApplication(){
        this.status = ProjectStatus.CLOSE;
    }

    public boolean shouldNotifyDeadline(){
        return status==ProjectStatus.CLOSE && !orgNotifiedOfDeadline;
    }

    public void markOrgNotifiedOfDeadline(){
        this.orgNotifiedOfDeadline = true;
    }

    public boolean getReviewable(){
        return LocalDate.now().plusDays(7).isAfter(this.getEndDate());
    }
}
