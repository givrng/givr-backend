package com.backend.givr.shared.repo;

import com.backend.givr.shared.entity.Creator;
import com.backend.givr.shared.entity.InitiativeApplication;
import com.backend.givr.shared.entity.ProjectApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface InitiativeApplicationRepo extends JpaRepository<InitiativeApplication, String> {
    Page<InitiativeApplication> findAllByProjectApplicationProjectCreator(Creator creator, PageRequest request);

    Optional<InitiativeApplication> findByProjectApplication(ProjectApplication projectApplication);

}
