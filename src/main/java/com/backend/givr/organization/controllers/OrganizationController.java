package com.backend.givr.organization.controllers;

import com.backend.givr.organization.dtos.CreateOrganizationDto;
import com.backend.givr.organization.dtos.OrganizationDashboard;
import com.backend.givr.organization.dtos.ProjectDto;

import com.backend.givr.organization.mappings.OrganizationMapper;
import com.backend.givr.organization.service.ApplicationService;
import com.backend.givr.organization.service.OrganizationService;
import com.backend.givr.shared.VolunteerApplicationDto;
import com.backend.givr.shared.enums.ApplicationStatus;
import com.backend.givr.shared.interfaces.SecurityDetails;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/api/organization")
public class OrganizationController {

    @Autowired
    private OrganizationMapper mapper;
    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private OrganizationService service;

    @PostMapping("/auth/signup")
    public ResponseEntity<Void> createVolunteerAccount(@RequestBody @Valid CreateOrganizationDto createOrganizationDto){
        service.createOrganization(createOrganizationDto);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/dashboard")
    public ResponseEntity<OrganizationDashboard> getOrganizationDashboard(@AuthenticationPrincipal SecurityDetails details){
        return ResponseEntity.ok(service.getOrganizationDashboard(details));
    }

    @GetMapping("/projects")
    public ResponseEntity<List<ProjectDto>> getOrganizationProjects(@AuthenticationPrincipal SecurityDetails details){
        return ResponseEntity.ok(mapper.toDtos(service.getProjects(details)));
    }

    @PostMapping("/projects")
    public ResponseEntity<ProjectDto> createProject(@RequestBody @Valid ProjectDto projectDto, @AuthenticationPrincipal SecurityDetails details){
        return ResponseEntity.ok(service.createProject(projectDto, details));
    }

    @GetMapping("/projects/applicants")
    public ResponseEntity<List<VolunteerApplicationDto>> getProjectApplication(@AuthenticationPrincipal SecurityDetails details){
        return ResponseEntity.ok(service.getProjectApplications(details));
    }

    @PatchMapping("/projects/application/{id}/accept")
    public ResponseEntity<Void> acceptApplication(@AuthenticationPrincipal SecurityDetails details, @PathVariable("id") Long id){
        applicationService.changeApplicationStatus(id, ApplicationStatus.APPROVED);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/projects/application/{id}/reject")
    public ResponseEntity<Void> rejectApplication(@PathVariable("id") Long id){
        applicationService.changeApplicationStatus(id, ApplicationStatus.REJECTED);
        return ResponseEntity.noContent().build();
    }
}
