package com.backend.givr.organization.repo;

import com.backend.givr.organization.entity.VerificationPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VerificationPaymentRepo extends JpaRepository<VerificationPayment, Long> {
    Optional<VerificationPayment> findByMerchant(String transactionRef);
}
