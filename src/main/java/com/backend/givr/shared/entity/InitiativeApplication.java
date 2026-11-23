package com.backend.givr.shared.entity;

import com.backend.givr.shared.entity.projects.Donation;
import com.backend.givr.shared.entity.projects.InitiativeItem;
import com.backend.givr.shared.entity.projects.Project;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class InitiativeApplication extends BaseEntity {

    @OneToOne
    private ProjectApplication projectApplication;

    @OneToMany(cascade = CascadeType.ALL)
    private final List<Donation> items = new ArrayList<>();
    @OneToMany(cascade = CascadeType.ALL)
    private final List<Donation> services = new ArrayList<>();
}
