package com.backend.givr.shared.entity.projects;

import com.backend.givr.shared.entity.BaseEntity;
import com.backend.givr.shared.enums.DonationStatus;
import com.backend.givr.shared.enums.ResourceKind;
import com.backend.givr.volunteer.entity.Volunteer;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.util.List;
import java.util.stream.Stream;

@Entity
@NoArgsConstructor
@Getter
@EqualsAndHashCode(of = "resource", callSuper = true)
@Table(uniqueConstraints = @UniqueConstraint(name = "unq_resource_initiative", columnNames = {"resource", "initiative", "kind"}))
public class InitiativeItem extends BaseEntity {
    @ManyToOne
    private Resource resource;

    @Setter
    private Integer quantityRequested;

    @ManyToOne(optional = false)
    private Initiative initiative;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResourceKind kind;

    private Integer totalDonated;

    @OneToMany(mappedBy = "item", fetch = FetchType.EAGER)
    private List<Donation> donations;

    public InitiativeItem(Resource commodity, int quantity, Initiative initiative, ResourceKind kind){
        this.resource = commodity;
        this.quantityRequested = quantity;
        this.initiative = initiative;
        this.kind = kind;
    }

    public Donation addDonation(Volunteer volunteer, int quantity){
        int totalDonated = switch (kind){
            // Donations for items are calculated based on fulfilled promises
            // This is because I assumed items will be sent to the organizer beforehand
            case ITEM -> filterDonationsBy(DonationStatus.Promise_Fulfilled)
                    .mapToInt(Donation::getQuantity).sum();

            // Donations for services will be calculated on the basis of accepted promises
            // Assumption made here is that a service can only be fulfilled on the day of the initiative
            case SERVICE -> filterDonationsBy(DonationStatus.Promised_Accepted)
                    .mapToInt(Donation::getQuantity).sum();
        };

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

    public void updateTotalDonated(int numberDonated){
        this.totalDonated += numberDonated;
    }

    private Stream<Donation> filterDonationsBy(DonationStatus status){
        return donations.stream()
                .filter(donation -> donation.getStatus().equals(status));
    }


}
