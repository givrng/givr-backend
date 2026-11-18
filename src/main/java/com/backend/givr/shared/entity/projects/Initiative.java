package com.backend.givr.shared.entity.projects;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@Getter
public class Initiative extends Project {

    @OneToMany(mappedBy = "initiative", cascade = {CascadeType.PERSIST, CascadeType.REFRESH})
    private final List<InitiativeItem> initiativeItems = new ArrayList<>();

    @OneToMany(mappedBy = "initiative", cascade = {CascadeType.PERSIST, CascadeType.REFRESH})
    private final List<InitiativeItem> initiativeServices = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "media", nullable = false)
    @Setter
    private Media media;
}
