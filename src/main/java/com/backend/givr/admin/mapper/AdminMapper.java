package com.backend.givr.admin.mapper;


import com.backend.givr.admin.dtos.ProjectReviewDto;
import com.backend.givr.admin.dtos.ProjectReviewResponseDto;
import com.backend.givr.admin.dtos.ReviewDto;
import com.backend.givr.admin.dtos.ReviewResponseDto;
import com.backend.givr.admin.entity.ProjectReview;
import com.backend.givr.admin.entity.Review;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AdminMapper {

    ReviewResponseDto toDto(Review review);

    ProjectReviewResponseDto toDto(ProjectReview review);
}
