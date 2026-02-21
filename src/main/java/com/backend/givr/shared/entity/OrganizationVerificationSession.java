package com.backend.givr.shared.entity;

import com.backend.givr.organization.dtos.OrganizationUpdateDto;
import com.backend.givr.organization.entity.Organization;
import com.backend.givr.organization.service.verify.Address;
import com.backend.givr.shared.enums.ReviewStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@Getter
@Setter
@Table(uniqueConstraints = @UniqueConstraint(name = "org_reg_unq", columnNames = {"organization_id", "claimed_cac_reg_number"}))
public class OrganizationVerificationSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long sessionId;

    @ManyToOne
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @Column(name = "claimed_cac_reg_number")
    private String claimedCACRegNumber;
    private String claimedOrgName;
    private String claimedType;
    @Column(nullable = false)
    @NotBlank
    @URL
    private String cacDocUrl;

    @Setter
    private String review;
    @Setter
    @ManyToOne
    private Location claimedLocation;
    private Address claimedAddress;

    @Enumerated(EnumType.STRING)
    @Setter
    private ReviewStatus reviewStatus;


    private LocalDateTime createdAt;
    @PrePersist
    private void setExpiresAt(){
        this.createdAt = LocalDateTime.now();
    }

    public OrganizationVerificationSession(Organization organization, OrganizationUpdateDto updateDto){
        this.organization = organization;
        this.claimedAddress = new Address(updateDto.getAddress(), updateDto.getLocation().getLga(), updateDto.getLocation().getState());
        this.claimedOrgName = updateDto.getName();
        this.claimedCACRegNumber = updateDto.getCacRegNumber();
        this.reviewStatus = ReviewStatus.Pending;
        this.claimedType = updateDto.getCategory().getFirst();
        this.cacDocUrl = updateDto.getCacDocUrl();
    }
}
