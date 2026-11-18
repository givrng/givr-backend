package com.backend.givr.shared.repo;

import com.backend.givr.shared.entity.projects.Media;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MediaRepo extends JpaRepository<Media, String> {
}
