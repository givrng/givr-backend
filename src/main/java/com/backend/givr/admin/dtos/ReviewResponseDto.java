package com.backend.givr.admin.dtos;

import com.backend.givr.admin.entity.Admin;
import com.backend.givr.shared.enums.ReviewStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ReviewResponseDto {
    private long reviewId;
    private String review;
    private Boolean isApproved;
    private Admin reviewedBy;
    private ReviewStatus status;
}
