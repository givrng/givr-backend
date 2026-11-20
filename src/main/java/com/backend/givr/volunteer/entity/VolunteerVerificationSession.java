package com.backend.givr.volunteer.entity;

import com.backend.givr.shared.enums.IDType;
import com.backend.givr.shared.enums.ReviewStatus;
import com.backend.givr.shared.enums.VerificationStatus;
import com.backend.givr.volunteer.dtos.VolunteerVerificationDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@NoArgsConstructor
@Getter
@Setter
@Table(uniqueConstraints = @UniqueConstraint(name = "unq_id", columnNames = {"idType", "idNumber"}))
public class VolunteerVerificationSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long verificationSessionId;

    @ManyToOne
    @JoinColumn(name = "volunteer_id")
    private Volunteer volunteer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IDType idType;

    @Column(nullable = false)
    private String idNumber;

    @Column(nullable = false)
    private String firstname;

    @Column(nullable = false)
    private String lastname;

    private ReviewStatus reviewStatus;

    private String middleName;
    private String individualLogo;
    private String remark;
    private LocalDate dateOfBirth;
    @Enumerated(EnumType.STRING)
    private VerificationStatus verificationStatus;

    public VolunteerVerificationSession(VolunteerVerificationDto verificationDto, Volunteer volunteer){
        this.idNumber = verificationDto.idNumber();
        this.idType = verificationDto.idType();
        verificationStatus = VerificationStatus.PENDING;
        this.firstname = verificationDto.firstname();
        this.lastname = verificationDto.lastname();
        this.middleName = verificationDto.middleName();
        this.volunteer = volunteer;
        this.reviewStatus = ReviewStatus.Pending;
        this.individualLogo = verificationDto.individualLogo();
        this.dateOfBirth = verificationDto.dateOfBirth();
    }

    public void update(VolunteerVerificationDto verificationDto){
        this.idNumber = verificationDto.idNumber();
        this.idType = verificationDto.idType();
        verificationStatus = VerificationStatus.PENDING;
        this.firstname = verificationDto.firstname();
        this.lastname = verificationDto.lastname();
        this.middleName = verificationDto.middleName();
        this.individualLogo = verificationDto.individualLogo();
        this.dateOfBirth = verificationDto.dateOfBirth();
    }
}
