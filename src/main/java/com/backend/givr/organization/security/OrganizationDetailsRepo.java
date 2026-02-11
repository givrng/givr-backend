package com.backend.givr.organization.security;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrganizationDetailsRepo extends JpaRepository<OrganizationDetails, Long> {
    Optional<OrganizationDetails> findByEmail(String email);

    boolean existsByEmail(String email);

}
