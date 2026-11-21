package com.backend.givr.volunteer.repo;

import com.backend.givr.shared.enums.IDType;
import com.backend.givr.shared.enums.ReviewStatus;
import com.backend.givr.volunteer.entity.Volunteer;
import com.backend.givr.volunteer.entity.VolunteerVerificationSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VolunteerVerificationSessionRepo extends JpaRepository<VolunteerVerificationSession, Long> {
    boolean existsByIdNumberAndIdType(String idNumber, IDType idType);

    Optional<VolunteerVerificationSession> findByVolunteer(Volunteer volunteer);

    Optional<VolunteerVerificationSession> findByIdTypeAndIdNumber(IDType type, String idNumber);
    Page<VolunteerVerificationSession> findByReviewStatus(ReviewStatus status, Pageable pageable);
}
