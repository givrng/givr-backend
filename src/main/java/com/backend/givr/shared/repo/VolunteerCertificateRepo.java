package com.backend.givr.shared.repo;

import com.backend.givr.shared.entity.VolunteerCertificate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VolunteerCertificateRepo extends JpaRepository<VolunteerCertificate, String> {
}
