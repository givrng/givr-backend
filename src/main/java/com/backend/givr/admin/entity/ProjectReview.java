package com.backend.givr.admin.entity;

import com.backend.givr.shared.entity.BaseEntity;
import com.backend.givr.shared.entity.projects.Project;
import com.backend.givr.shared.enums.ReviewStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
@AllArgsConstructor
public class ProjectReview extends BaseEntity {
    @Column(nullable = false)
    private String review;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewStatus status;

    @ManyToOne(optional = false)
    private Project project;

    @ManyToOne(optional = false)
    private Admin reviewedBy;

}
