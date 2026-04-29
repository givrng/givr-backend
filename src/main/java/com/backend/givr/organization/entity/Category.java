package com.backend.givr.organization.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class Category {
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    private Short categoryId;

    private String category;
    public Category(String category){
        this.category= category;
    }
}
