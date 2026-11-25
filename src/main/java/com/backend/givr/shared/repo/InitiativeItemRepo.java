package com.backend.givr.shared.repo;

import com.backend.givr.shared.entity.projects.Initiative;
import com.backend.givr.shared.entity.projects.InitiativeItem;
import com.backend.givr.shared.entity.projects.Resource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InitiativeItemRepo extends JpaRepository<InitiativeItem, String> {
    Optional<InitiativeItem> findByResourceAndInitiative(Resource resource, Initiative initiative);
}
