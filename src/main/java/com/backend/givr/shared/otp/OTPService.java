package com.backend.givr.shared.otp;

import com.backend.givr.shared.enums.AccountType;
import com.backend.givr.shared.enums.OTPStatus;
import com.backend.givr.shared.enums.OtpPurpose;
import com.backend.givr.shared.exceptions.InvalidOtpException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class OTPService {
    @Autowired
    private OTPRepo repo;

    @Value("${OTP.SECRETS}")
    private String otpSecret;

    public OTP generateOtp(String email, String token, AccountType accountType, OtpPurpose purpose){
        Optional<OTP> existing = repo.findByEmailAndAccountTypeAndPurpose(email, accountType, purpose);
        if(existing.isPresent() && existing.get().getExpiresAt().isAfter(LocalDateTime.now()) && !existing.get().getIsUsed() )
            return existing.get();

        existing.ifPresent(repo::delete);

        OTP otp = new OTP(email, generateHash(token),OTPGenerator.DURATION, accountType);
        otp.setPurpose(purpose);
        return repo.save(otp);
    }
    @Transactional
    public void markAsSent(OTP otp, String emailId){
        otp.markAsSent(emailId);
        repo.save(otp);
    }

    public void deleteOtp(OTP otp){
        repo.delete(otp);
    }
    @Transactional
    public void verifyOtp(String email, String token, AccountType accountType, OtpPurpose purpose){
        String otpHash = generateHash(token);
        OTP otp = repo.findValidOtp(email, otpHash, accountType, purpose).orElseThrow(()->new InvalidOtpException("Invalid or expired OTP"));
        otp.setIsUsed(true);
    }

    public boolean tokenExistsAndValid(String email, AccountType accountType, OtpPurpose purpose){
        Optional<OTP> optionalOTP = repo.findByEmailAndAccountTypeAndPurpose(email, accountType, purpose);
        if(optionalOTP.isEmpty()){
           return false;
        }
        OTP otp = optionalOTP.get();
        return otp.getExpiresAt().isAfter(LocalDateTime.now());
    }
    private void invalidateExistingOtp(String email, AccountType accountType, OtpPurpose purpose){
        repo.markAllUsed(email, accountType, purpose);
    }

    private String generateHash(String otp){
        return DigestUtils.md5DigestAsHex((otp + otpSecret).getBytes(StandardCharsets.UTF_8));
    }
}
