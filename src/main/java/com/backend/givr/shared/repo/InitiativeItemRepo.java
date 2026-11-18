package com.backend.givr.shared.repo;

import com.backend.givr.shared.entity.projects.InitiativeItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InitiativeItemRepo extends JpaRepository<InitiativeItem, String> {
}
