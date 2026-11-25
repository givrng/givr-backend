package com.backend.givr.shared.entity.projects;

import com.backend.givr.organization.entity.AttendanceHours;
import com.backend.givr.organization.entity.Category;
import com.backend.givr.organization.entity.Organization;
import com.backend.givr.shared.entity.*;
import com.backend.givr.shared.enums.ProjectStatus;
import com.backend.givr.shared.enums.ReviewStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


@Getter
@Setter
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@SQLRestriction("is_deleted=false")
@Table(uniqueConstraints = @UniqueConstraint(name = "unq_creator_title", columnNames = {"creator", "title"}))
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long projectId;

    @ManyToOne
    @JoinColumn(name = "organization_id")
    protected Organization organization;

    @Column(nullable = false)
    protected String title;

    @ManyToOne
    @JoinColumn(name = "creator_id")
    protected Creator creator;

    @Column(nullable = false, length = 1000)
    protected String description;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "project_categories",
            joinColumns = @JoinColumn(name = "project_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    protected List<Category> categories = new ArrayList<>();

    protected String category;

    @Min(value = 1, message = "Cannot create a project for no volunteer")
    @Column(nullable = false)
    protected Integer maxVolunteers;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "location", nullable = false)
    protected Location location;

    @Column(nullable = false)
    protected LocalDate startDate;
    @Column(nullable = false)
    protected LocalDate endDate;
    @Column(nullable = false)
    protected LocalDate deadline;

    @OneToOne(mappedBy = "project")
    @JoinColumn(name = "receipt_id")
    protected ProjectPaymentReceipt creationReceipt;

    protected String address;

    @Embedded
    protected AttendanceHours attendanceHours;

    @Enumerated(EnumType.STRING)
    protected ProjectStatus status;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "project_skills", joinColumns = @JoinColumn(name = "project_id"), inverseJoinColumns = @JoinColumn(name = "skill_id"))
    protected Set<Skill> requiredSkills;

    @Column(length = 300)
    protected String specialRequirements;

    protected String organizerName;

    protected Boolean reviewable;

    @Enumerated(EnumType.STRING)
    protected ReviewStatus reviewStatus;

    protected boolean isDeleted = false;
    protected Double rating;
    protected String projectFlierUrl;
    protected String projectCardUrl;
    protected String shareableLink;
    protected ZonedDateTime createdAt;
    protected ZonedDateTime modifiedAt;


    @OneToMany(mappedBy = "project")
    protected Set<ProjectApplication> applicationList;

    @OneToMany(mappedBy = "project")
    @SQLRestriction(value = "status = 'APPROVED'")
    protected Set<ProjectApplication> approvedList;

    private Boolean orgNotifiedOfDeadline;


    public void markOrgNotifiedOfDeadline(){
        this.orgNotifiedOfDeadline = true;
    }
    public boolean shouldNotifyDeadline(){
        return status==ProjectStatus.CLOSE && !orgNotifiedOfDeadline;
    }
    public int getVolunteerCount(){
        return this.approvedList==null? 0 : approvedList.size();
    }
    @PrePersist
    protected void setCreatedAt(){
        this.createdAt = ZonedDateTime.now(ZoneOffset.UTC);
    }
    @PreUpdate
    protected void setModifiedAt(){
        this.modifiedAt = ZonedDateTime.now(ZoneOffset.UTC);
    }

    public Boolean getReviewable(){
        return LocalDate.now().plusDays(7).isAfter(this.endDate);
    }

    public boolean shouldClose(LocalDateTime now){
        return status != ProjectStatus.CLOSE && now.isAfter(deadline.atTime(23, 59, 59));
    }

    @PostLoad
    public void updateCategories(){
        if(this.category != null && categories.isEmpty())
            categories.add(new Category(category));
    }
    public void closeApplication(){
        this.status = ProjectStatus.CLOSE;
    }

}
