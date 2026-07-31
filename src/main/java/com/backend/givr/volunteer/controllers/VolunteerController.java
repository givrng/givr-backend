package com.backend.givr.volunteer.controllers;

import com.backend.givr.organization.dtos.OrganizationDto;
import com.backend.givr.organization.dtos.ProjectDto;
import com.backend.givr.organization.service.ApplicationService;
import com.backend.givr.organization.service.OrganizationService;
import com.backend.givr.organization.service.ProjectService;
import com.backend.givr.shared.ProjectApplicationForm;
import com.backend.givr.shared.interfaces.SecurityDetails;
import com.backend.givr.volunteer.dtos.CreateVolunteerRequestDto;
import com.backend.givr.volunteer.dtos.VolunteerDashboard;
import com.backend.givr.volunteer.dtos.VolunteerProfile;
import com.backend.givr.volunteer.security.VolunteerDetails;
import com.backend.givr.volunteer.service.VolunteerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/api/volunteer")
public class VolunteerController {
    @Autowired
    private VolunteerService service;
    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private ProjectService projectService;
    @PostMapping("/auth/signup")
    public ResponseEntity<Void> createVolunteerAccount(@RequestBody @Validated CreateVolunteerRequestDto payload){
        var createdVolunteer = service.createAccount(payload);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/dashboard")
    public ResponseEntity<VolunteerDashboard> getVolunteerDashboard(@AuthenticationPrincipal SecurityDetails volunteerDetails){
        return ResponseEntity.ok(service.getVolunteerDashboard(volunteerDetails.getId()));
    }

    @GetMapping("/profile")
    public ResponseEntity<VolunteerProfile> getVolunteerProfile(@AuthenticationPrincipal SecurityDetails volunteerDetails){
        return ResponseEntity.ok(service.getVolunteerProfile(volunteerDetails.getId()));
    }
    @PostMapping("/projects/apply")
    public ResponseEntity<Void> applyForProject(@AuthenticationPrincipal VolunteerDetails volunteerDetails, @RequestBody @Valid ProjectApplicationForm applicationForm){
        service.apply(volunteerDetails.getId(), applicationForm);
        return ResponseEntity.ok().build();
    }
    @GetMapping("/projects")
    public ResponseEntity<List<ProjectDto>> getAvailableProjects(){
        return ResponseEntity.ok(projectService.getAllProjectsForVolunteer());
    }
    @GetMapping("/organizations")
    public ResponseEntity<List<OrganizationDto>> getOrganizationDtoResponseEntity(){
        return ResponseEntity.ok(organizationService.getOrganizations());
    }
}
