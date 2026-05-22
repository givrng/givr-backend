package com.backend.givr.shared.repo;

import com.backend.givr.shared.entity.GivrUserProjectPointer;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GivrUserProjectPointerRepo extends JpaRepository<GivrUserProjectPointer, String> {
    Optional<GivrUserProjectPointer> findFirstBy(Sort savedAt);
}
