package com.backend.givr.shared.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class InitiativeRequestDto extends ProjectRequestDto {
    // Items
    private List<InitiativeResourceDto> initiativeItemList;
    // Service
    private List<InitiativeResourceDto> initiativeServiceList;
    // Media
    @NotNull
    private MediaDto media;
}
