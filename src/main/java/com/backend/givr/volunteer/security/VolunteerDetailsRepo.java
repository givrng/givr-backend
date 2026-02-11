package com.backend.givr.volunteer.security;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VolunteerDetailsRepo extends JpaRepository<VolunteerDetails, Long> {
    Optional<VolunteerDetails> findByEmail(String email);

    boolean existsByEmail(String email);
}
