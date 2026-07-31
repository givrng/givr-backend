package com.backend.givr.shared.dtos;

import com.backend.givr.shared.entity.Location;
import com.backend.givr.shared.entity.OrganizationVerificationSession;
import com.backend.givr.shared.enums.IDType;
import com.backend.givr.shared.enums.ReviewStatus;
import com.backend.givr.shared.enums.VerificationStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

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
        String idNumber;
        IDType idType;

        LocalDate contactDateOfBirth;
        String contactFullName;
        VerificationStatus verificationStatus;
        String remark;

        public Claims(OrganizationVerificationSession verificationSession){
            this.organizationName = verificationSession.getClaimedOrgName();
            Location l = verificationSession.getClaimedLocation();
            this.address = l!=null?String.format("%s, %s, %s", verificationSession.getClaimedAddress(), l.getLga(), l.getState()): String.format("%s, %s, %s", verificationSession.getClaimedAddress(), "","");
            this.cacRegNumber = verificationSession.getClaimedCACRegNumber();
            this.idNumber = verificationSession.getIdNumber();
            this.idType = verificationSession.getIdType();
            this.contactFullName = String.format("%S, %s", verificationSession.getContactLastname(), verificationSession.getContactFirstname());
            this.verificationStatus = verificationSession.getVerificationStatus();
            this.remark = verificationSession.getRemark();

           this.contactDateOfBirth = verificationSession.getDateOfBirth();
        }
    }

    private String id;
    private String name;
    private Claims submitted;
    private String cacDocumentImageUrl;
    private ReviewStatus reviewStatus;
    private String reviewNote;


    public VerificationSessionDto(OrganizationVerificationSession verificationSession){
        this.id = String.valueOf(verificationSession.getSessionId());
        this.name = verificationSession.getOrganization().getContactFirstname();
        this.reviewStatus = verificationSession.getReviewStatus();
        this.cacDocumentImageUrl = verificationSession.getCacDocUrl();
        this.submitted = new Claims(verificationSession);

        this.reviewNote = verificationSession.getReview();
    }
}
