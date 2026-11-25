package com.backend.givr.shared.dtos;

import com.backend.givr.shared.entity.projects.Resource;
import com.backend.givr.shared.enums.ResourceKind;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.List;

@Data
public class InitiativeItemDto {
    private String id;
    private Resource resource;
    private int quantityRequested;

    private MinimalInitiativeDto initiative;
    private ResourceKind kind;

    List<DonationDto> donations;

    private ZonedDateTime createdAt;
}
