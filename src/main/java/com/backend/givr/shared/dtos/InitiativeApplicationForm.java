package com.backend.givr.shared.dtos;

import com.backend.givr.shared.enums.ApplicationType;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class InitiativeApplicationForm extends ProjectApplicationForm{
    ApplicationType applicationType;
    List<InitiativeResourceDto> items;
    List<InitiativeResourceDto> services;
}
