package com.backend.givr.shared.entity.projects;

import com.backend.givr.shared.entity.BaseEntity;
import com.backend.givr.shared.enums.DonationStatus;
import com.backend.givr.volunteer.entity.Volunteer;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@NoArgsConstructor
@Getter
@EqualsAndHashCode(of = "resource", callSuper = true)
public class InitiativeItem extends BaseEntity {
    @ManyToOne
    private Resource resource;

    private int quantityRequested;

    @ManyToOne(optional = false)
    private Initiative initiative;

    @OneToMany(mappedBy = "item", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<Donation> donations;

    public InitiativeItem(Resource commodity, int quantity, Initiative initiative){
        this.resource = commodity;
        this.quantityRequested = quantity;
        this.initiative = initiative;
    }

    public Donation addDonation(Volunteer volunteer, int quantity){
        int totalDonated = donations.stream()
                .filter(donation -> donation.getStatus().equals(DonationStatus.Promise_Fulfilled))
                .mapToInt(Donation::getQuantity).sum();

        if(totalDonated == quantityRequested)
            return null;

        int newTotal = totalDonated + quantity;

        int diff = quantityRequested - newTotal;
        Donation donation;
        if(diff > 0 ){
            donation = new Donation(volunteer, this, quantity, DonationStatus.Pending_acceptance);
            donations.add(donation);
            return donation;
        }else if(diff < 0 && quantity + diff > 0){
            quantity += diff;
            donation = new Donation(volunteer, this, quantity, DonationStatus.Pending_acceptance);
            donations.add(donation);
            return donation;
        }
        return null;
    }
}
