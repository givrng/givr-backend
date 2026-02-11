package com.backend.givr.organization.entity;

import com.backend.givr.shared.Location;
import com.backend.givr.shared.enums.ProjectStatus;
import com.backend.givr.shared.enums.VerificationStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.URL;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@ToString
@Setter
@NoArgsConstructor
public class Organization {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String organizationId;

    @NotBlank
    @Column(nullable = false)
    private String contactFirstname;

    private String contactMiddleName;
    @NotBlank
    @Column(nullable = false)
    private String contactLastname;

    private String phoneNumber;

    private String organizationName;

    private String organizationType;

    @Column(unique = true)
    private String cacRegNumber;

    @ManyToOne
    @JoinColumn(name = "location_id")
    private Location location;

    private String address;

    @Column(length = 500)
    private String description;

    private String website;
    @URL
    private String profileUrl;

    @Enumerated(EnumType.STRING)
    private VerificationStatus status;

    private Boolean emailVerified;

    private Boolean profileCompleted;
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "organization", orphanRemoval = true, cascade = CascadeType.ALL)
    private Set<Project> projects;

    public void addProject(Project project){
        project.setStatus(ProjectStatus.DRAFT);
        project.setOrganization(this);
        this.projects.add(project);
    }
    //    Get recently created projects
    public List<Project> getProjects(){
        return this.projects.stream().sorted(Comparator.comparing(Project::getCreatedAt)).toList();
    }

    @Transient
    public int getNumOfActiveProjects(){
        return projects == null? 0: projects.stream().filter(p->p.getStatus()==ProjectStatus.OPEN || p.getStatus() == ProjectStatus.ONGOING).toList().size();
    }
}
