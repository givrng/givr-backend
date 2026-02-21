package com.backend.givr.organization.repo;

import com.backend.givr.organization.entity.CACResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrganizationCACResponseRepo extends JpaRepository<CACResponse, String> {
}
