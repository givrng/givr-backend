package com.backend.givr.admin.repos;

import com.backend.givr.admin.entity.Admin;
import com.backend.givr.shared.enums.ReviewStatus;
import jakarta.validation.constraints.Email;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminRepo extends JpaRepository<Admin, String> {
    Optional<Admin> findByEmail(@Email String email);

    boolean existsByEmail(String email);
}
