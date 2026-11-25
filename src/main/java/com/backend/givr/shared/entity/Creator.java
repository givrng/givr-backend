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

    private Integer freeProjectCount;

    public Creator(Volunteer individual, int freeInitiativeCount){
        this.individual = individual;
        this.freeProjectCount = freeInitiativeCount;
    }

    public Creator(Organization organization){
        this.organization = organization;
        this.freeProjectCount = 0;
    }

    public void useFreeProject(){
        if(freeProjectCount == null)
            freeProjectCount = 0;
        else freeProjectCount -= 1;
    }

    public boolean hasFreeProject(){
        return freeProjectCount != null && freeProjectCount > 0;
    }
}
