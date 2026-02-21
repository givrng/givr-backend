package com.backend.givr.shared.repo;

import com.backend.givr.organization.entity.Organization;
import com.backend.givr.shared.entity.OrganizationVerificationSession;
import com.backend.givr.shared.enums.ReviewStatus;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrganizationVerificationSessionRepo extends JpaRepository<OrganizationVerificationSession, Long> {
    Optional<OrganizationVerificationSession> findByOrganization(Organization organization);

    @Nullable List<OrganizationVerificationSession> findByReviewStatus(ReviewStatus status, Sort sort);
}
