package com.backend.givr.admin.dtos;

import com.backend.givr.admin.entity.Admin;
import com.backend.givr.shared.enums.ReviewStatus;

public record ProjectReviewResponseDto(
        String id,
        String review,
        Boolean isApproved,
        Admin reviewedBy,
        ReviewStatus status
) {
}
