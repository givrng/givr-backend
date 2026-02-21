package com.backend.givr.shared.repo;

import com.backend.givr.shared.entity.GivrTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GivrTransactionRepo extends JpaRepository<GivrTransaction, String> {
}
