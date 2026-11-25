package com.backend.givr.shared.entity.projects;
import com.backend.givr.shared.dtos.MinimalInitiativeDto;
import com.backend.givr.shared.enums.ReviewStatus;
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

    @OneToMany(mappedBy = "initiative", cascade = {CascadeType.ALL})
    @SQLRestriction("kind = 'ITEM' ")
    private final List<InitiativeItem> initiativeItems = new ArrayList<>();

    @OneToMany(mappedBy = "initiative", cascade = {CascadeType.ALL})
    @SQLRestriction("kind = 'SERVICE' ")
    private final List<InitiativeItem> initiativeServices = new ArrayList<>();

    @Setter
    @OneToOne( optional = false, cascade = CascadeType.ALL)
    private Media media;

    public MinimalInitiativeDto toMinimalDto(){
        return new MinimalInitiativeDto(
                projectId,
                title,
                createdAt
        );
    }
}
