package com.backend.givr.volunteer.repo;

import com.backend.givr.volunteer.entity.Individual;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IndividualRepo extends JpaRepository<Individual, String> {
}
