package com.backend.givr.shared.entity;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@MappedSuperclass
@Getter
@EqualsAndHashCode(of = {"id"})
public abstract class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    protected String id;

    protected ZonedDateTime createdAt;
    protected ZonedDateTime updatedAt;

    @PrePersist
    private void setCreatedAt(){
        this.createdAt = ZonedDateTime.now(ZoneId.of("Africa/Lagos"));
    }

    @PreUpdate
    private void setUpdatedAt(){
        this.updatedAt = ZonedDateTime.now(ZoneId.of("Africa/Lagos"));
    }
}
