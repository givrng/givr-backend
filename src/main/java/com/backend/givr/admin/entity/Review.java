package com.backend.givr.admin.entity;

import com.backend.givr.shared.entity.OrganizationVerificationSession;
import com.backend.givr.shared.enums.ReviewStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZoneId;

@NoArgsConstructor
@Entity
@Getter
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long reviewId;

    @Setter
    private String review;
    @Setter
    private Boolean isApproved;

    @ManyToOne
    private Admin reviewedBy;

    @ManyToOne
    private OrganizationVerificationSession verificationSession;

    @Enumerated(EnumType.STRING)
    @Setter
    private ReviewStatus status;

    private LocalDateTime createdAt;

    @PrePersist
    private void setCreatedAt(){
        this.createdAt = LocalDateTime.now(ZoneId.of("Africa/Lagos"));
    }

    public Review(String review, Boolean isApproved, Admin reviewedBy, OrganizationVerificationSession session){
        this.status = isApproved? ReviewStatus.Approved : ReviewStatus.Rejected;
        this.isApproved = isApproved;
        this.review = review;
        this.reviewedBy = reviewedBy;
        this.verificationSession = session;
    }
}
