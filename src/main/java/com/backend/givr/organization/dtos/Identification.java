package com.backend.givr.organization.dtos;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
@Embeddable
public class Identification {
    @Column( name = "id_type")
    private String type;
    @Column(name = "id_number")
    private String id;
}
