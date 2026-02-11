package com.backend.givr.organization.repo;

import com.backend.givr.organization.entity.Organization;
import com.backend.givr.organization.entity.Project;
import com.backend.givr.shared.enums.ProjectStatus;
import com.backend.givr.volunteer.entity.Volunteer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ProjectRepo extends JpaRepository<Project, Long> {
    List<Project> findAllByOrganizationOrderByCreatedAtAsc(Organization organization);

    Optional<Project> findByProjectIdAndOrganization(Long projectId, Organization organization);

    List<Project> findAllByStatus(ProjectStatus projectStatus);

    @Query("""
            SELECT p from Project p
            WHERE p.deadline < :today
            """)
    List<Project> findExpiredProjects(LocalDate today);

    @Query("""
    SELECT p
    FROM Project p
    WHERE p.location.state = :state
    AND p.status = :status
    AND EXISTS (
        SELECT 1
        FROM Volunteer v
        JOIN v.skills vs
        JOIN p.requiredSkills ps
        WHERE v = :volunteer
        AND ps = vs
    )
""")
    List<Project> findProjectsWithAnyMatchingSkill(Volunteer volunteer, String state, ProjectStatus status);

    List<Project> findAllByOrganizationAndStatus(Organization organization, ProjectStatus status);
}
