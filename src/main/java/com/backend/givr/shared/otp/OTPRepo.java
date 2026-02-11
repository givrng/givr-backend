package com.backend.givr.shared.otp;

import com.backend.givr.shared.enums.AccountType;
import com.backend.givr.shared.enums.OtpPurpose;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface OTPRepo extends JpaRepository<OTP, Long> {

    @Query("""
            SELECT o FROM OTP o
            WHERE o.email = :email
            AND o.accountType = :accountType
            AND o.otpHash = :otpHash
            AND o.isUsed = false
            AND o.purpose = :purpose
            AND o.expiresAt > CURRENT_TIMESTAMP
            """)
    Optional<OTP> findValidOtp(String email, String otpHash, AccountType accountType, OtpPurpose purpose);

    @Modifying
    @Transactional
    @Query("""
            UPDATE OTP o
            SET o.isUsed = true
            WHERE o.email = :email
            AND o.accountType = :accountType
            AND o.purpose = :purpose
            AND o.isUsed = false
            """)
    int markAllUsed(String email, AccountType accountType, OtpPurpose purpose);

    Optional<OTP> findByEmailAndAccountTypeAndPurpose(String email, AccountType accountType, OtpPurpose purpose);
}
