package com.backend.givr.shared.repo;

import com.backend.givr.shared.entity.projects.Resource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ResourceRepo extends JpaRepository<Resource, String> {
    Optional<Resource> findByName(String trim);
}
