package com.backend.givr.shared.entity.projects;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@Getter
public class Initiative extends Project {

    @OneToMany(mappedBy = "initiative", cascade = {CascadeType.PERSIST, CascadeType.REFRESH})
    @SQLRestriction("kind = 'ITEM' ")
    private final List<InitiativeItem> initiativeItems = new ArrayList<>();

    @OneToMany(mappedBy = "initiative", cascade = {CascadeType.PERSIST, CascadeType.REFRESH})
    @SQLRestriction("kind = 'SERVICE' ")
    private final List<InitiativeItem> initiativeServices = new ArrayList<>();

    @Setter
    @OneToOne( optional = false)
    private Media media;

}
