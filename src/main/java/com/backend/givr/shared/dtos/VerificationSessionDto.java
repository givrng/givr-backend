package com.backend.givr.shared.dtos;

import com.backend.givr.organization.service.verify.Address;
import com.backend.givr.shared.entity.OrganizationVerificationSession;
import com.backend.givr.shared.enums.ReviewStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class VerificationSessionDto {
    @Getter
    @Setter
    @NoArgsConstructor
    public static class Claims {
        String organizationName;
        String address;
        String cacRegNumber;

        public Claims(OrganizationVerificationSession verificationSession){
            this.organizationName = verificationSession.getClaimedOrgName();
            Address address1 = verificationSession.getClaimedAddress();
            this.address = String.format("%s, %s, %s", address1.address(), address1.LGA(), address1.state());
            this.cacRegNumber = verificationSession.getClaimedCACRegNumber();

        }
    }

    String id;
    String name;
    Claims submitted;
    String cacDocumentImageUrl;
    ReviewStatus status;
    String reviewNote;

    public VerificationSessionDto(OrganizationVerificationSession verificationSession){
        this.id = String.valueOf(verificationSession.getSessionId());
        this.name = verificationSession.getOrganization().getContactFirstname();
        this.status = verificationSession.getReviewStatus();
        this.cacDocumentImageUrl = verificationSession.getCacDocUrl();
        this.submitted = new Claims(verificationSession);
        this.reviewNote = verificationSession.getReview();
    }
}
