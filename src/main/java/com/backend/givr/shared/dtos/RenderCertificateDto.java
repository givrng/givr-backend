package com.backend.givr.shared.dtos;

import com.backend.givr.organization.entity.Participation;
import com.backend.givr.organization.entity.Project;
import com.backend.givr.volunteer.entity.Volunteer;
import lombok.Data;

import java.time.LocalDate;

@Data
public class RenderCertificateDto{
    private String certId;
    private String firstName;
    private String lastName;
    private String orgName;
    private String projectTitle;
    private LocalDate startDate;
    private LocalDate endDate;
    private String organizationLogo;
    private String impactArea;

    public RenderCertificateDto(Project project, Volunteer volunteer, String certId){
        this.certId = certId;
        this.firstName = volunteer.getFirstname();
        this.lastName = volunteer.getLastname();
        this.projectTitle = project.getTitle();
        this.startDate = project.getStartDate();
        this.endDate = project.getEndDate();
        this.organizationLogo = project.getOrganization().getProfileUrl();
        this.orgName = project.getOrganization().getOrganizationName();
        this.impactArea = String.format("%s", project.getCategories().getFirst().getCategory());
    }
}
