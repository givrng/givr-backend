package com.backend.givr.shared.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;

@Entity
@Getter
@Setter
@Table(uniqueConstraints = @UniqueConstraint(name = "email_token_unq", columnNames = {"email", "tokenId"}))
@NoArgsConstructor
public class TokenId {
    @Id
    private String tokenId;

    private String email;
    @Setter
    private boolean isUsed;
    @Setter
    private boolean isRevoked;

    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    public TokenId(String email, String tokenId){
        this.email = email;
        this.tokenId = tokenId;
        this.isUsed = false;
        this.isRevoked = false;
    }

    public void revoke(){
        this.isRevoked = true;
    }
    @PrePersist
    private void setCreatedAt(){
        this.createdAt = ZonedDateTime.now();
    }

    @PreUpdate
    private void setUpdatedAt(){
        this.updatedAt = ZonedDateTime.now();
    }
}
