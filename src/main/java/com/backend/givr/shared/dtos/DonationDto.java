package com.backend.givr.shared.dtos;

import com.backend.givr.shared.enums.DonationStatus;
import lombok.Data;

@Data
public class DonationDto {
    private String id;
    private String itemName;
    private InitiativeResponseDto initiative;
    private DonationStatus status;
    private int quantityDonated;
}
