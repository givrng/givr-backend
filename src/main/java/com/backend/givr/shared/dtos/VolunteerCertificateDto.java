package com.backend.givr.shared.dtos;

import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class VolunteerCertificateDto {
    private String certId;
    private String certUrl;
    private String organizationName;
    private String projectTitle;
    private ZonedDateTime issuedAt;
}
