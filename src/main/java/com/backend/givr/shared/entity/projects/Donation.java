package com.backend.givr.shared.entity.projects;

import com.backend.givr.shared.entity.BaseEntity;
import com.backend.givr.shared.enums.DonationStatus;
import com.backend.givr.volunteer.entity.Volunteer;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.SQLUpdate;


@Entity
@NoArgsConstructor
@Getter
@AllArgsConstructor
@SQLRestriction("""
    not exists (
        select 1
        from initiative_item ii
        join project p on p.project_id = ii.initiative_project_id
        where ii.id = item_id
          and p.status = 'DRAFT'
    )
""")
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
