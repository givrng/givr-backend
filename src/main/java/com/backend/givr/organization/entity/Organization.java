package com.backend.givr.organization.entity;

import com.backend.givr.shared.Location;
import com.backend.givr.shared.enums.OrganizationType;
import com.backend.givr.shared.enums.ProjectStatus;
import com.backend.givr.shared.enums.VerificationStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.URL;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@ToString
@Setter
public class Organization {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String organizationId;

    @NotBlank
    private String contactFirstname;

    private String contactMiddleName;
    @NotBlank
    private String contactLastname;

    @NotBlank
    @Column(nullable = false)
    private String phoneNumber;

    @NotBlank
    @Column(nullable = false)
    private String organizationName;

    private String organizationType;

    @Column(unique = true)
    private String cacRegNumber;

    @NotBlank
    private String driversLicenseNumber;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;
    private String description;

    @URL
    private String website;

    @Enumerated(EnumType.STRING)
    private VerificationStatus status;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "organization", orphanRemoval = true, cascade = CascadeType.ALL)
    private final Set<Project> projects;

    public Organization(){
        this.projects = new HashSet<>();
    }

    public void addProject(Project project){
        project.setStatus(ProjectStatus.DRAFT);
        project.setOrganization(this);
        this.projects.add(project);
    }
    //    Get recently created projects
    public List<Project> getProjects(){
        return this.projects.stream().sorted(Comparator.comparingInt(p -> p.getCreatedAt().getNano())).toList();
    }

}
