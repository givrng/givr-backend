package com.backend.givr.shared.entity.projects;

import com.backend.givr.shared.entity.BaseEntity;
import com.backend.givr.shared.enums.DonationStatus;
import com.backend.givr.volunteer.entity.Volunteer;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@NoArgsConstructor
@Getter
@AllArgsConstructor
public class Donation extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    private Volunteer donor;

    @ManyToOne(fetch = FetchType.LAZY)
    private InitiativeItem item;

    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Setter
    private DonationStatus status;

    public void increateTotalDonated() {
        item.updateTotalDonated(quantity);
    }

    public void reduceTotalDonated(){
        item.updateTotalDonated(-quantity);
    }
}
