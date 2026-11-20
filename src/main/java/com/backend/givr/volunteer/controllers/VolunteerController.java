package com.backend.givr.volunteer.controllers;

import com.backend.givr.organization.dtos.CheckoutResponse;
import com.backend.givr.shared.dtos.ProjectResponseDto;
import com.backend.givr.organization.service.OrganizationService;
import com.backend.givr.shared.dtos.*;
import com.backend.givr.shared.enums.MediaUploadPurpose;
import com.backend.givr.shared.service.*;
import com.backend.givr.shared.entity.GivrMessage;
import com.backend.givr.shared.enums.OtpPurpose;
import com.backend.givr.shared.interfaces.SecurityDetails;
import com.backend.givr.shared.enums.AuthProviderType;
import com.backend.givr.shared.mapper.ProjectMapper;
import com.backend.givr.shared.otp.OtpDto;
import com.backend.givr.volunteer.dtos.*;
import com.backend.givr.volunteer.entity.Volunteer;
import com.backend.givr.volunteer.security.VolunteerDetails;
import com.backend.givr.volunteer.service.VolunteerService;
import jakarta.annotation.security.PermitAll;
import jakarta.persistence.EntityManager;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/${api.version}/api/volunteer")
@RequiredArgsConstructor
public class VolunteerController {
    private final VolunteerService service;
    private final OrganizationService organizationService;
    private final ParticipationService participationService;
    private final ProjectMapper projectMapper;
    private final LogoutService logoutService;
    private final GivrMessageService messageService;
    private final EntityManager em;

    private final ProjectService projectService;
    private final InitiativeService initiativeService;

    private final CloudinaryService cloudinaryService;

    @GetMapping("/media/upload/signature")
    public ResponseEntity<MediaUploadResponse> generateUploadSignature(@RequestParam(name = "purpose") MediaUploadPurpose purpose){
        return ResponseEntity.ok(cloudinaryService.generateSignedUploadKey(purpose));
    }

    @PostMapping("/auth/signup")
    public ResponseEntity<Void> createVolunteerAccount(@RequestBody @Validated CreateVolunteerRequestDto payload){
        service.createAccount(payload);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me/details")
    public ResponseEntity<AuthDetailsDto> getAuthDetails(@AuthenticationPrincipal SecurityDetails details){
        return ResponseEntity.ok(new AuthDetailsDto(details.getId(), details.getUsername()));
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

    @GetMapping("/profile/image")
    @PermitAll
    public ResponseEntity<URL> getProfileUrl (@RequestParam("userId") String volunteerId) throws URISyntaxException {
        URL profileUrl = service.getVolunteerProfile(volunteerId).getProfileUrl();
        return ResponseEntity.status(HttpStatus.FOUND).location(profileUrl.toURI()).build();
    }

    @PatchMapping("/profile")
    public ResponseEntity<VolunteerProfile> updateVolunteerProfile(@AuthenticationPrincipal SecurityDetails details, @RequestBody UpdateVolunteerDto profile){
        return ResponseEntity.ok(service.updateProfile(details.getId(), profile, details));
    }

    @PostMapping("/profile/initiate/verification")
    public ResponseEntity<CheckoutResponse> initiateAccountVerification(@RequestBody @Valid VolunteerVerificationDto verificationDto, @AuthenticationPrincipal SecurityDetails securityDetails){
        return ResponseEntity.ok(service.initiateVerification(verificationDto, securityDetails));
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
        service.applyToProject(volunteerDetails, applicationForm);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/initiative/apply")
    public ResponseEntity<Void> applyForInitiative(@AuthenticationPrincipal VolunteerDetails volunteerDetails, @RequestBody @Valid InitiativeApplicationForm applicationForm){
        service.applyToInitiative(volunteerDetails, applicationForm);
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/projects")
    public ResponseEntity<PagedModel<ProjectResponseDto>> getAvailableProjects(@RequestParam(name = "pageNum", defaultValue = "0") int pageNum,
                                                                               @RequestParam(name = "pageSize", defaultValue = "30") int pageSize){
        PageRequest pageRequest = PageRequest.of(pageNum, pageSize);
        return ResponseEntity.ok(projectService.getAllProjectsForVolunteer(pageRequest));
    }

    @GetMapping("/initiative/{initiativeId}/resources")
    public ResponseEntity<InitiativeResourceResponseDto> getInitiativeResources(@PathVariable("initiativeId") Long initiativeId){
        return ResponseEntity.ok(initiativeService.getInitiativeResources(initiativeId));
    }

    @GetMapping("/projects/recommended")
    public ResponseEntity<List<ProjectResponseDto>> getRecommendedProjects(@AuthenticationPrincipal SecurityDetails details){
        return ResponseEntity.ok(service.getRecommendedProjects(details));
    }

    @GetMapping("/chat/{projectId}/history")
    public ResponseEntity<PagedModel<GivrMessage>> loadConversation(@PathVariable(name = "projectId") Long projectId, @RequestParam(name = "cursor",
            defaultValue = "", required = false) String cursor, @RequestParam(name = "size", required = false, defaultValue = "40") int size){
        return ResponseEntity.ok(new PagedModel<>(messageService.loadConversation(projectId, cursor, size)));
    }

    @GetMapping("/projects/unread")
    public ResponseEntity<Map<Long, Long>> getUnreadCount (@AuthenticationPrincipal SecurityDetails details){
        return ResponseEntity.ok(messageService.computeUserChatUnreadCount(details.getId()));
    }

    @GetMapping("/organizations")
    public ResponseEntity<List<OrganizationResponseDTOv>> getOrganizationDtoResponseEntity(){
        return ResponseEntity.ok(organizationService.getOrganizations());
    }

    @GetMapping("/organizations/{orgId}/projects/active")
    public ResponseEntity<List<ProjectViewResponse>> getOrganizationActiveProjects(@PathVariable("orgId") String orgId){
        return ResponseEntity.ok(projectService.getActiveProjectsByOrganization(orgId));
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
