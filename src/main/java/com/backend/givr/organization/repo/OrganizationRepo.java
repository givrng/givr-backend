package com.backend.givr.organization.repo;

import com.backend.givr.organization.entity.Organization;
import com.backend.givr.shared.enums.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrganizationRepo extends JpaRepository<Organization, String> {
    boolean existsByCacRegNumber(String claimedCacRegNumber);

    List<Organization> findAllByStatus(VerificationStatus verificationStatus);
}
