package com.backend.givr.shared;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@NoArgsConstructor
@Setter
@Table(uniqueConstraints = @UniqueConstraint(name = "unq_state_lga", columnNames = {"state", "lga"}) )
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String state;
    private String lga;
}
