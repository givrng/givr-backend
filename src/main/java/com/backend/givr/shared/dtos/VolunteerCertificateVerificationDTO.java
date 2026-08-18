package com.backend.givr.shared.dtos;

import lombok.Data;

@Data
public class VolunteerCertificateVerificationDTO {
    private VolunteerCertificateDto certificate;
    private String volunteerFirstName;
    private String volunteerLastName;
    private String volunteerMiddleName;
}
