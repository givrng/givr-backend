package com.backend.givr.shared.entity;

import com.backend.givr.organization.entity.Project;
import com.backend.givr.volunteer.entity.Volunteer;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(name = "unq_volunteer_project", columnNames = {"project", "volunteer"}))
@NoArgsConstructor
@Getter
public class Rating {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long ratingId;

    @ManyToOne
    @Setter
    private Volunteer volunteer;

    @Setter
    @ManyToOne
    private Project project;

    @Column(nullable = false)
    @Setter
    private Double rating;

    private LocalDateTime createdAt;

    @PrePersist
    private void setCreatedAt(){
        this.createdAt = LocalDateTime.now();
    }

    public Rating(Volunteer volunteer, Project project, Double rating){
        this.volunteer = volunteer;
        this.project = project;
        this.rating = rating;
    }
}
