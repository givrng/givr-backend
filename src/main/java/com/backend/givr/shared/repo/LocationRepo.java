package com.backend.givr.shared.repo;

import com.backend.givr.shared.Location;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LocationRepo extends JpaRepository<Location, Integer> {
    Optional<Location> findByStateAndLga(String state, String lga);
}
