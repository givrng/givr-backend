package com.backend.givr.shared.repo;

import com.backend.givr.organization.entity.Organization;
import com.backend.givr.organization.entity.Project;
import com.backend.givr.shared.entity.Rating;
import com.backend.givr.volunteer.entity.Volunteer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RatingRepo extends JpaRepository<Rating, Long> {
    @Query("SELECT AVG(r.rating) FROM Rating r WHERE r.project = :project")
    Double getProjectRating(@Param("project") Project project);

    @Query("""
    SELECT COALESCE(AVG(r.rating), 0)
    FROM Rating r
    WHERE r.project.organization = :organization
    """)
    Double getOrganizationRating(@Param("organization") Organization organization);

    Optional<Rating> findByVolunteerAndProject(Volunteer volunteer, Project project);
}
