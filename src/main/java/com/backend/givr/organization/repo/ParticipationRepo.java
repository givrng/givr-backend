package com.backend.givr.organization.repo;

import com.backend.givr.organization.entity.Organization;
import com.backend.givr.shared.dtos.ParticipationDto;
import com.backend.givr.shared.entity.Participation;
import com.backend.givr.shared.entity.Project;
import com.backend.givr.shared.enums.CertificationStatus;
import com.backend.givr.shared.enums.ParticipationStatus;
import com.backend.givr.volunteer.entity.Individual;
import com.backend.givr.volunteer.entity.Volunteer;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ParticipationRepo extends JpaRepository<Participation, Long> {
    List<Participation> findAllByVolunteer(Volunteer volunteer);

    Optional<Participation> findByIdAndVolunteer(Long participationId, Volunteer volunteer);

    Optional<Participation> deleteByIdAndVolunteer(Long participationId, Volunteer volunteer);

    List<Participation> findAllByOrganization(Organization organization);

    Optional<Participation> findByVolunteerAndProject(Volunteer volunteer, Project project);

    List<Participation> findAllByProject(Project project);

    Page<Participation> findAllByCertificationStatusAndParticipationStatus(CertificationStatus certificationStatus, ParticipationStatus participationStatus, Pageable pageable);

    @Nullable List<Participation> findAllByProjectIndividual(Individual individual);

    Optional<Participation> findByIdAndOrganization(Long participationId, Organization organization);

    Optional<Participation> findByIdAndProjectIndividual(Long participationId, Individual individual);

}
