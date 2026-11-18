package com.backend.givr.shared.entity.projects;

import com.backend.givr.shared.entity.BaseEntity;
import jakarta.persistence.Entity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@NoArgsConstructor
@Getter
@EqualsAndHashCode(of = {"name"}, callSuper = false)
public class Resource extends BaseEntity {
    private String name;

    public Resource(String name){
        this.name = name;
    }
}
