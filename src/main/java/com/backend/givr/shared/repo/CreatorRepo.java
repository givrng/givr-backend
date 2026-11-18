package com.backend.givr.shared.repo;

import com.backend.givr.organization.entity.Organization;
import com.backend.givr.shared.entity.Creator;
import com.backend.givr.volunteer.entity.Volunteer;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.swing.text.html.Option;
import java.util.Optional;

public interface CreatorRepo extends JpaRepository<Creator, String> {
    Optional<Creator> findByIndividual(Volunteer individual);

    Optional<Creator> findByOrganization(Organization organization);

    Optional<Creator> findByIndividualVolunteerId(String individualId);

}
