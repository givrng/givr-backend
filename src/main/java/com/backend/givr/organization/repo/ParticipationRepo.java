package com.backend.givr.organization.repo;

import com.backend.givr.organization.entity.Organization;
import com.backend.givr.organization.entity.Participation;
import com.backend.givr.volunteer.entity.Volunteer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ParticipationRepo extends JpaRepository<Participation, Long> {
    List<Participation> findAllByVolunteer(Volunteer volunteer);

    Optional<Participation> findByIdAndVolunteer(Long participationId, Volunteer volunteer);

    Optional<Participation> deleteByIdAndVolunteer(Long participationId, Volunteer volunteer);

    List<Participation> findAllByOrganization(Organization organization);

}
