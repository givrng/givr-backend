package com.backend.givr.shared.dtos;

import jakarta.validation.Valid;
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
    private List<@Valid InitiativeResourceDto> initiativeItemList;
    // Service
    private List<@Valid InitiativeResourceDto> initiativeServiceList;
    // Media
    @NotNull
    private MediaDto media;
}
