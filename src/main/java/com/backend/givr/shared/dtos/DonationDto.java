package com.backend.givr.shared.dtos;

import com.backend.givr.shared.enums.DonationStatus;
import com.backend.givr.volunteer.dtos.VolunteerDto;
import lombok.Data;

@Data
public class DonationDto {
    private String id;
    private String itemName;
    private VolunteerDto donor;
    private MinimalInitiativeDto initiative;
    private DonationStatus status;
    private int quantityDonated;

}
