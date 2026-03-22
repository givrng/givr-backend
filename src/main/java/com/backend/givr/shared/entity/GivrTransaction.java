package com.backend.givr.shared.entity;

import com.backend.givr.shared.enums.Merchant;
import com.backend.givr.shared.enums.TransactionType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Getter
@NoArgsConstructor
public class GivrTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String transactionId;

    private Merchant merchant;
    private String transactionRef;
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    private LocalDateTime createdAt;

    @PrePersist
    private void setCreatedAt(){
        this.createdAt = LocalDateTime.now(ZoneId.of("Africa/Lagos"));
    }

    public GivrTransaction(Merchant merchant, String transactionRef, BigDecimal amount, TransactionType transactionType){
        this.merchant = merchant;
        this.transactionRef = transactionRef;
        this.amount = amount;

        this.transactionType = transactionType;
    }
}
