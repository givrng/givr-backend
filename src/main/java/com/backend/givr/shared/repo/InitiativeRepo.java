package com.backend.givr.shared.repo;

import com.backend.givr.shared.entity.Creator;
import com.backend.givr.shared.entity.projects.Initiative;
import com.backend.givr.shared.enums.ProjectStatus;
import com.backend.givr.volunteer.entity.Volunteer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface InitiativeRepo extends JpaRepository<Initiative, Long> {
    Page<Initiative> findAllByCreator(Creator creator, Pageable pageRequest);

    List<Initiative> findAllByCreatorAndStatus(Creator individual, ProjectStatus status);

    Page<Initiative> findAllByCreatorAndStatus(Creator individual, ProjectStatus status, Pageable pageable);

    Optional<Initiative> findByProjectIdAndCreator(Long initiativeId, Creator creator);

    @Query("""
            SELECT p from Initiative p
            WHERE p.deadline < :today
            """)
    List<Initiative> findExpiredInitiatives(LocalDate today);

    @Query("""
            SELECT p from Initiative p
            WHERE :weekFromToday >  p.endDate
            """)
    List<Initiative> findClosingInitiatives(@Param("weekFromToday") LocalDate today);

    Optional<Initiative> findByProjectIdAndCreatorIndividual(Long projectId, Volunteer individual);

    int countByCreatorAndStatusNot(Creator creator, ProjectStatus projectStatus);

    Page<Initiative> findAllByStatus(ProjectStatus projectStatus, PageRequest pageRequest);

    Optional<Initiative> findByProjectIdAndStatusNot(Long initiativeId, ProjectStatus status);
}
