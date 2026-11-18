package com.backend.givr.organization.repo;

import com.backend.givr.organization.entity.Organization;
import com.backend.givr.shared.entity.Participation;
import com.backend.givr.shared.entity.projects.Project;
import com.backend.givr.shared.enums.CertificationStatus;
import com.backend.givr.shared.enums.ParticipationStatus;
import com.backend.givr.volunteer.entity.Volunteer;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ParticipationRepo extends JpaRepository<Participation, Long> {
    List<Participation> findAllByVolunteer(Volunteer volunteer);

    Optional<Participation> findByIdAndVolunteer(Long participationId, Volunteer volunteer);

    Optional<Participation> deleteByIdAndVolunteer(Long participationId, Volunteer volunteer);

    List<Participation> findAllByOrganization(Organization organization);

    Optional<Participation> findByVolunteerAndProject(Volunteer volunteer, Project project);

    List<Participation> findAllByProjectProjectId(Long projectId);

    Page<Participation> findAllByCertificationStatusAndParticipationStatus(CertificationStatus certificationStatus, ParticipationStatus participationStatus, Pageable pageable);

    @Nullable List<Participation> findAllByProjectCreatorIndividual(Volunteer individual);

    Optional<Participation> findByIdAndOrganization(Long participationId, Organization organization);

    Optional<Participation> findByIdAndCreatorIndividual(Long participationId, Volunteer individual);

    @Query(value = """
            SELECT p from Participation p 
            LEFT JOIN p.creator c 
            LEFT JOIN c.organization co 
            WHERE co=:organization 
            OR p.organization = :organization""", countQuery = """
            SELECT COUNT(p) FROM Participation p
            LEFT JOIN p.creator c
            LEFT JOIN c.organization co
            WHERE co = :organization
            OR p.organization = :organization
            """)
    Page<Participation> getAllByCreatorOrOrganization(@Param("organization") Organization organization, Pageable pageable);

    Optional<Participation> findByIdAndOrganizationOrCreatorOrganization(Long participationId, Organization organization, Organization org);
}
