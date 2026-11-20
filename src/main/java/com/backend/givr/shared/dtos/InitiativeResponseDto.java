package com.backend.givr.shared.dtos;

import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
public class InitiativeResponseDto extends ProjectResponseDto{
    private List<InitiativeResourceDto> services;
    private List<InitiativeResourceDto> items;
    private MediaDto media;
}
