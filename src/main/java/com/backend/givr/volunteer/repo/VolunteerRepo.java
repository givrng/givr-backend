package com.backend.givr.volunteer.repo;

import com.backend.givr.shared.enums.VerificationStatus;
import com.backend.givr.volunteer.entity.Volunteer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VolunteerRepo extends JpaRepository<Volunteer, String> {
    Optional<Volunteer> findByEmail(String username);

    boolean existsByEmail(String email);

    List<Volunteer> findAllByLocationState(String state);

    Optional<Volunteer> findByVolunteerIdAndVerificationStatus(String volunteerId, VerificationStatus verificationStatus);
}
