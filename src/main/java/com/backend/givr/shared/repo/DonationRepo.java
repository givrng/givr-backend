package com.backend.givr.shared.repo;

import com.backend.givr.shared.entity.Creator;
import com.backend.givr.shared.entity.projects.Donation;
import com.backend.givr.volunteer.entity.Volunteer;
import jakarta.persistence.LockModeType;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface DonationRepo extends JpaRepository<Donation, String> {
    Page<Donation> findAllByDonor(Volunteer volunteer, PageRequest pageRequest);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Donation> findByIdAndItemInitiativeCreator(@NotBlank String id, Creator creator);

    Page<Donation> findAllByItemInitiativeCreator(Creator creator, PageRequest pageRequest);
}
