package com.backend.givr.shared.repo;

import com.backend.givr.shared.entity.InitiativeApplication;
import com.backend.givr.shared.enums.ApplicationStatus;
import com.backend.givr.volunteer.entity.Volunteer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface InitiativeApplicationRepo extends JpaRepository<InitiativeApplication, String> {
}
