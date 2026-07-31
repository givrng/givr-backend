package com.backend.givr.organization.repo;

import com.backend.givr.organization.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationRepo extends JpaRepository<Organization, String> {
}
