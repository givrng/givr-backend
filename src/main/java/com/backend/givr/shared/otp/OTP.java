package com.backend.givr.shared.otp;

import com.backend.givr.shared.enums.AccountType;
import com.backend.givr.shared.enums.OTPStatus;
import com.backend.givr.shared.enums.OtpPurpose;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.DigestUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@NoArgsConstructor
@Getter
@Table(indexes = {
        @Index(name = "idx_otp_lookup", columnList = "email, accountType, purpose, otpHash"),
        @Index(name = "idx_otp_expiry", columnList = "expiresAt")
})
public class OTP {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String otpHash;

    @Column(nullable = false)
    private String email;

    @Setter
    private String resendEmailId;

    @Setter
    @Column(nullable = false)
    private Boolean isUsed;

    @Enumerated(EnumType.STRING)
    private OTPStatus status;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AccountType accountType;

    @Column(nullable = false)
    private Duration duration;

    @Setter
    @Enumerated(EnumType.STRING)
    private OtpPurpose purpose;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime expiresAt;

    private LocalDateTime sentAt;

    public OTP(String email, String otpHash, Duration duration, AccountType accountType){
        this.otpHash = otpHash;
        this.duration = duration;
        this.email = email;
        this.accountType = accountType;
        this.isUsed = false;
        this.status = OTPStatus.PENDING;
    }

    @PreUpdate()
    private void setTimeline(){
        if(status == OTPStatus.SENT){
            this.sentAt = LocalDateTime.now(ZoneId.of("Africa/Lagos"));
            this.expiresAt = this.createdAt.plus(this.duration);
        }
    }

    @PrePersist()
    private  void setCreatedAt(){
        this.createdAt = LocalDateTime.now(ZoneId.of("Africa/Lagos"));
    }

    public void markAsSent(String resendEmailId){
        this.status = OTPStatus.SENT;
        this.resendEmailId = resendEmailId;
    }
        public boolean isSent(){
        return status == OTPStatus.SENT;
    }
}
