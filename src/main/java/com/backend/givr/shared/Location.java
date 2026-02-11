package com.backend.givr.shared;

import com.backend.givr.organization.dtos.LocationDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@NoArgsConstructor
@Setter
@Table(indexes = @Index(name = "idx_state", columnList = "state"), uniqueConstraints = @UniqueConstraint(name = "unq_state_lga", columnNames = {"state", "lga"} ))
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String state;
    private String lga;

    public Location(LocationDto locationDto){
        this.state = locationDto.getState();
        this.lga = locationDto.getLga();
    }
}
