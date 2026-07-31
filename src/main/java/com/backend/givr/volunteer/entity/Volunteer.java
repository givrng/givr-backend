package com.backend.givr.volunteer.entity;

import com.backend.givr.shared.Location;
import com.backend.givr.shared.Skill;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;


@Entity
@Getter
@Setter
public class Volunteer {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String volunteerId;
    private String firstname;
    private String middleName;
    private String lastname;

    private String phoneNumber;
    @ManyToOne
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @ManyToMany(cascade = CascadeType.MERGE)
    @JoinTable(name = "volunteer_skills", joinColumns = @JoinColumn(name = "volunteer_id"), inverseJoinColumns = @JoinColumn(name = "skill_id"))
    private Set<Skill> skills = new HashSet<>();

    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    @PrePersist
    private void setCreatedAt(){
        this.createdAt = ZonedDateTime.now();
    }

    @PreUpdate
    private void setUpdatedAt() {
        this.updatedAt = ZonedDateTime.now();
    }
}
