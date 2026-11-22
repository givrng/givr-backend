package com.backend.givr.shared.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class InitiativeResourceDto{
    private String resource;
    private String resourceId;
    private int quantity;
}
