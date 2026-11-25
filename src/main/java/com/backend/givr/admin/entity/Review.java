package com.backend.givr.admin.entity;

import com.backend.givr.shared.entity.OrganizationVerificationSession;
import com.backend.givr.shared.enums.ReviewStatus;
import com.backend.givr.volunteer.entity.VolunteerVerificationSession;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZoneId;

@NoArgsConstructor
@Entity
@Getter
@Table(check = @CheckConstraint(name = "check_no_two_sessions", constraint = "verification_session IS NOT null AND volunteer_verification_session IS null OR " +
        "volunteer_verification_session IS NOT null AND verification_session is null"))
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
    @JoinColumn(name = "verification_session")
    private OrganizationVerificationSession verificationSession;

    @ManyToOne
    @JoinColumn(name = "volunteer_verification_session")
    private VolunteerVerificationSession volunteerVerificationSession;
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

    public Review(String review, Boolean isApproved, Admin reviewedBy, VolunteerVerificationSession session){
        this.status = isApproved? ReviewStatus.Approved : ReviewStatus.Rejected;
        this.isApproved = isApproved;
        this.review = review;
        this.reviewedBy = reviewedBy;
        this.volunteerVerificationSession = session;
    }
}
