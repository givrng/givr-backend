package com.backend.givr.admin.repos;

import com.backend.givr.admin.entity.ProjectReview;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectReviewRepo extends JpaRepository<ProjectReview, String> {
}
