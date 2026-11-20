package com.backend.givr.shared.dtos;

import com.backend.givr.shared.enums.IDType;
import com.backend.givr.shared.enums.VerificationStatus;
import com.backend.givr.volunteer.entity.VolunteerVerificationSession;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class VolunteerVerificationSessionDto {
    private Long verificationSessionId;

    private IDType idType;

    private String idNumber;

    private String firstname;
    private String lastname;
    private String middleName;
    private String individualLogo;
    private String remark;
    private LocalDate dateOfBirth;
    private VerificationStatus verificationStatus;

    public VolunteerVerificationSessionDto(VolunteerVerificationSession session){
        this.verificationSessionId = session.getVerificationSessionId();
        this.idNumber = session.getIdNumber();
        this.idType = session.getIdType();
        this.firstname = session.getFirstname();
        this.middleName = session.getMiddleName();
        this.lastname = session.getLastname();
        this.dateOfBirth = session.getDateOfBirth();
        this.verificationStatus = session.getVerificationStatus();
    }
}
