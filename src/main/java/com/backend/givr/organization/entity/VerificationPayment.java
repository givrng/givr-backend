package com.backend.givr.organization.entity;

import com.backend.givr.shared.dtos.TransactionStatus;
import com.backend.givr.shared.enums.Merchant;
import com.backend.givr.shared.enums.PaymentMethod;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

@Entity
@NoArgsConstructor
@Getter
@Table(indexes = {@Index(unique = true, name = "ind_merchant", columnList = "merchantRefId")})
public class VerificationPayment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;

    @ManyToOne
    private Organization organization;

    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    @Column(nullable = false)
    private BigDecimal amount;

    @Setter
    private BigDecimal amountPaid;

    private LocalDateTime createdAt;

    private String description;

    @Setter
    private String merchantRefId;

    @Column(nullable = false)
    private String transactionRef;

    @Setter
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Merchant merchant;

    private LocalDateTime updateAt;

    @PrePersist
    private void setCreatedAt(){
        this.createdAt = LocalDateTime.now(ZoneId.of("Africa/Lagos"));
    }

    @PreUpdate
    private void setUpdateAt(){
        this.updateAt = LocalDateTime.now(ZoneId.of("Africa/Lagos"));
    }

    public VerificationPayment(BigDecimal amount, String description, Organization organization){
        this.status = TransactionStatus.PENDING;
        this.amount = amount;
        this.description = description;
        this.merchant = Merchant.PAYSTACK;
        this.organization = organization;
        this.transactionRef = UUID.randomUUID().toString();
    }

    public void updateStatus(TransactionStatus status){
        if(this.status != TransactionStatus.PENDING)
            return;
        this.status = status;
    }
}
