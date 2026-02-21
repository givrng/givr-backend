package com.backend.givr.admin.repos;

import com.backend.givr.admin.entity.Review;
import com.backend.givr.shared.entity.OrganizationVerificationSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewRepo extends JpaRepository<Review, Long> {
    Optional<Review> findFirstByVerificationSessionOrderByCreatedAt(OrganizationVerificationSession session);

}
