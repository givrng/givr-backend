package com.backend.givr.volunteer.controllers;

import com.backend.givr.organization.dtos.ProjectResponseDto;
import com.backend.givr.organization.service.OrganizationService;
import com.backend.givr.organization.service.ParticipationService;
import com.backend.givr.organization.service.ProjectService;
import com.backend.givr.shared.dtos.ParticipationDto;
import com.backend.givr.shared.dtos.PasswordUpdateDto;
import com.backend.givr.shared.dtos.ProjectApplicationForm;
import com.backend.givr.shared.dtos.RatingDTO;
import com.backend.givr.shared.enums.OtpPurpose;
import com.backend.givr.shared.interfaces.SecurityDetails;
import com.backend.givr.shared.enums.AuthProviderType;
import com.backend.givr.shared.mapper.ProjectMapper;
import com.backend.givr.shared.otp.OtpDto;
import com.backend.givr.shared.service.LogoutService;
import com.backend.givr.volunteer.dtos.*;
import com.backend.givr.volunteer.entity.Volunteer;
import com.backend.givr.volunteer.security.VolunteerDetails;
import com.backend.givr.volunteer.service.VolunteerService;
import jakarta.persistence.EntityManager;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/${api.version}/api/volunteer")
public class VolunteerController {
    @Autowired
    private VolunteerService service;
    @Autowired
    private OrganizationService organizationService;
    @Autowired
    private ParticipationService participationService;
    @Autowired
    private ProjectMapper projectMapper;
    @Autowired
    private LogoutService logoutService;
    @Autowired
    private EntityManager em;

    @Autowired
    private ProjectService projectService;
    @PostMapping("/auth/signup")
    public ResponseEntity<Void> createVolunteerAccount(@RequestBody @Validated CreateVolunteerRequestDto payload){
        service.createAccount(payload);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/dashboard")
    public ResponseEntity<VolunteerDashboard> getVolunteerDashboard(@AuthenticationPrincipal SecurityDetails volunteerDetails){
        return ResponseEntity.ok(service.getVolunteerDashboard(volunteerDetails.getId()));
    }

    @GetMapping("/profile")
    public ResponseEntity<VolunteerProfile> getVolunteerProfile(@AuthenticationPrincipal SecurityDetails volunteerDetails){
        var profile = service.getVolunteerProfile(volunteerDetails.getId());
        profile.setEmail(volunteerDetails.getUsername());
        profile.setEmailEditable(volunteerDetails.getProviderType() == AuthProviderType.LOCAL);
        return ResponseEntity.ok(profile);
    }

    @PatchMapping("/profile")
    public ResponseEntity<VolunteerProfile> updateVolunteerProfile(@AuthenticationPrincipal SecurityDetails details, @RequestBody UpdateVolunteerDto profile){
        return ResponseEntity.ok(service.updateProfile(details.getId(), profile, details));
    }


    @GetMapping("/share/project/{id}")
    public ResponseEntity<String> shareProject(@PathVariable("id") Long projectId){
        return ResponseEntity.ok(service.shareProject(projectId));
    }
    @GetMapping("/projects/{id}")
    public ResponseEntity<ProjectResponseDto> getProject(@PathVariable("id") Long projectId){
        return ResponseEntity.ok(projectMapper.toProjectDto(projectService.getProject(projectId)));
    }
    @PostMapping("/projects/apply")
    public ResponseEntity<Void> applyForProject(@AuthenticationPrincipal VolunteerDetails volunteerDetails, @RequestBody @Valid ProjectApplicationForm applicationForm){
        service.apply(volunteerDetails, applicationForm);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/projects")
    public ResponseEntity<List<ProjectResponseDto>> getAvailableProjects(){
        return ResponseEntity.ok(projectService.getAllProjectsForVolunteer());
    }

    @GetMapping("/projects/recommended")
    public ResponseEntity<List<ProjectResponseDto>> getRecommendedProjects(@AuthenticationPrincipal SecurityDetails details){
        return ResponseEntity.ok(service.getRecommendedProjects(details));
    }

    @GetMapping("/organizations")
    public ResponseEntity<List<OrganizationResponseDTOv>> getOrganizationDtoResponseEntity(){
        return ResponseEntity.ok(organizationService.getOrganizations());
    }

    @GetMapping("/volunteering")
    public ResponseEntity<List<ParticipationDto>> getMyVolunteering(@AuthenticationPrincipal SecurityDetails details){
        return ResponseEntity.ok(service.getMyVolunteering(details));
    }

    /**
     * Create a review for a project participated in*/
    @PostMapping("/volunteering/{participationId}/review")
    public ResponseEntity<Void> createProjectReview(@PathVariable("participationId") Long participationId, @AuthenticationPrincipal SecurityDetails details,@RequestBody RatingDTO payload){
        Volunteer volunteer = em.getReference(Volunteer.class, details.getId());
        participationService.createRating(participationId, volunteer, payload);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/volunteering/{participationId}")
    public ResponseEntity<Void> rejectParticipation(@PathVariable("participationId") Long participationId, @AuthenticationPrincipal SecurityDetails details){
        service.rejectParticipation(participationId, details.getId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/otp/request")
    public ResponseEntity<Void> verifyEmail(@AuthenticationPrincipal SecurityDetails details, @RequestParam("purpose")OtpPurpose purpose){
        service.requestOtp( details.getUsername(), purpose);
        return ResponseEntity.accepted().build();
    }

    @PatchMapping("/verify/email")
    public ResponseEntity<Void> confirmEmail(@RequestBody @Valid OtpDto otpDto, @AuthenticationPrincipal SecurityDetails details){
        service.confirmEmail(details, otpDto.otp());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/password/update")
    public ResponseEntity<Void> updateVolunteerPassword(@AuthenticationPrincipal SecurityDetails details, @RequestBody PasswordUpdateDto passwordUpdateDto){
        service.updatePassword(details, passwordUpdateDto);
        return ResponseEntity.noContent().build();
    }
    @PostMapping("/logout")
    ResponseEntity<Void> logout(@AuthenticationPrincipal SecurityDetails authUser) {
        return ResponseEntity.ok().headers(logoutService.logout(authUser)).build();
    }
}
