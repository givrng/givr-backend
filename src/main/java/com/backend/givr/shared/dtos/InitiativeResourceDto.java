package com.backend.givr.shared.dtos;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class InitiativeResourceDto {
    private String resource;
    private String resourceId;
    @Min(value = 1, message = "Must request for at least 1 resource")
    private int quantity;
}
