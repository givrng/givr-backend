package com.backend.givr.shared.dtos;

import com.backend.givr.organization.entity.Participation;
import com.backend.givr.organization.entity.Project;
import com.backend.givr.volunteer.entity.Volunteer;

import java.time.LocalDate;

public class RenderCertificateDto{
    String certId;
    String firstName;
    String lastName;
    String projectTitle;
    LocalDate startDate;
    LocalDate endDate;
    String organizationLogo;
    String impactArea;

    public RenderCertificateDto(Project project, Volunteer volunteer, String certId){
        this.certId = certId;
        this.firstName = volunteer.getFirstname();
        this.lastName = volunteer.getLastname();
        this.projectTitle = project.getTitle();
        this.startDate = project.getStartDate();
        this.endDate = project.getEndDate();
        this.organizationLogo = project.getOrganization().getProfileUrl();
        this.impactArea = String.format("%s", project.getCategories().getFirst());
    }
}
