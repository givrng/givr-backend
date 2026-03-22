package com.backend.givr.shared.entity;

import com.backend.givr.organization.dtos.OrganizationUpdateDto;
import com.backend.givr.organization.entity.Organization;
import com.backend.givr.organization.service.verify.Address;
import com.backend.givr.shared.enums.IDType;
import com.backend.givr.shared.enums.ReviewStatus;
import com.backend.givr.shared.enums.VerificationStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDate;
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

    @OneToOne
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
    // Contact person verification information
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private IDType idType;
    @Column(nullable = false)
    private String idNumber;
    @URL
    @Column
    private String usrImgUrl;

    private String remark;
    private String contactFirstname;
    private String contactLastname;
    private LocalDate dateOfBirth;
    @Setter
    private String review;
    @Setter
    @ManyToOne
    private Location claimedLocation;
    private Address claimedAddress;

    @Enumerated(EnumType.STRING)
    @Setter
    private ReviewStatus reviewStatus;
    @Enumerated(EnumType.STRING)
    private VerificationStatus verificationStatus;

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
        this.idType = updateDto.getContactVerification().idType();
        this.idNumber = updateDto.getContactVerification().idNumber();
        this.usrImgUrl = updateDto.getContactVerification().usrImgUrl();
        this.verificationStatus = VerificationStatus.AUTOMATIC_VERIFICATION_PENDING;
        this.setContactFirstname(updateDto.getContactFirstname());
        this.setContactLastname(updateDto.getContactLastname());

        if(updateDto.getDateOfBirth() != null){
            this.setDateOfBirth( LocalDate.parse(updateDto.getDateOfBirth()));
        }
    }
}
