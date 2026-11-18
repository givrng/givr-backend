package com.backend.givr.shared.entity;

import com.backend.givr.organization.entity.Organization;
import com.backend.givr.volunteer.entity.Volunteer;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

/*
* A project can now be created by individuals.
* This class acts as a wrapper for project creators */
@Entity
@NoArgsConstructor
@Getter
public class Creator {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String creatorId;

    @OneToOne
    private Volunteer individual;
    @OneToOne
    private Organization organization;

    public Creator(Volunteer individual){
        this.individual = individual;
    }

    public Creator(Organization organization){
        this.organization = organization;
    }
}
