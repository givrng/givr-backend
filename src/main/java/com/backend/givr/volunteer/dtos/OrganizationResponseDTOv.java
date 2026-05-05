package com.backend.givr.volunteer.dtos;

import com.backend.givr.organization.dtos.LocationDto;
import com.backend.givr.shared.enums.VerificationStatus;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OrganizationResponseDTOv {
    private String organizationId;
    private String name;
    private String description;
    private LocationDto location;
    private List<String> category;
    private VerificationStatus status;
    private int numOfActiveProjects;
    private String website;
    private String address;
    private List<ProjectViewResponse> activeProjects;
    private double rating;
    private String profileUrl;
    private String organizationType;

}
