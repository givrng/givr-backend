package com.backend.givr.organization.controllers;

import com.backend.givr.organization.dtos.*;

import com.backend.givr.organization.service.ApplicationService;
import com.backend.givr.organization.service.OrganizationService;
import com.backend.givr.shared.dtos.ParticipationDto;
import com.backend.givr.shared.dtos.PasswordUpdateDto;
import com.backend.givr.shared.dtos.VolunteerApplicationDto;
import com.backend.givr.shared.entity.GivrMessage;
import com.backend.givr.shared.enums.OtpPurpose;
import com.backend.givr.shared.interfaces.SecurityDetails;
import com.backend.givr.shared.mapper.ProjectMapper;
import com.backend.givr.shared.otp.OtpDto;
import com.backend.givr.shared.service.GivrMessageService;
import com.backend.givr.shared.service.LogoutService;
import com.backend.givr.volunteer.dtos.AuthDetailsDto;
import com.resend.core.exception.ResendException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/${api.version}/api/organization")
public class OrganizationController {

    @Autowired
    private ProjectMapper mapper;
    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private OrganizationService service;

    @Autowired
    private LogoutService logoutService;

    @Autowired
    private GivrMessageService messageService;
    @PostMapping("/auth/signup")
    public ResponseEntity<Void> createOrganizationAccount(@RequestBody @Valid CreateOrganizationDto createOrganizationDto){
        service.createOrganization(createOrganizationDto);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me/details")
    public ResponseEntity<AuthDetailsDto> getAuthDetails(@AuthenticationPrincipal SecurityDetails details){
        return ResponseEntity.ok(new AuthDetailsDto(details.getId(), details.getUsername()));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<OrganizationDashboard> getOrganizationDashboard(@AuthenticationPrincipal SecurityDetails details){
        return ResponseEntity.ok(service.getOrganizationDashboard(details));
    }

    @GetMapping("/profile")
    public ResponseEntity<OrganizationProfileDto> getOrganizationProfile(@AuthenticationPrincipal SecurityDetails details){
        return ResponseEntity.ok(service.getOrganizationProfile(details));
    }

    @GetMapping("/profile/image")
    public ResponseEntity<String> getProfileImageUrl(@RequestParam("userId") String orgId){
        String orgLogo = service.getProfileUrl(orgId);
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(orgLogo)).build();
    }

    @PostMapping("/verification/initiate")
    public ResponseEntity<CheckoutResponse> initiateOrganizationVerification(@RequestBody OrganizationUpdateDto organizationDto, @AuthenticationPrincipal SecurityDetails details){
        return ResponseEntity.ok(service.initiateOrganizationVerification(organizationDto, details));
    }

    @PatchMapping("/profile")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<OrganizationProfileDto> updateOrganizationProfile(@RequestBody OrganizationUpdateDto organizationDto, @AuthenticationPrincipal SecurityDetails details){
        return ResponseEntity.ok(service.updateEmailOrLogo(organizationDto, details));
    }

    @GetMapping("/profile/email/exists")
    public ResponseEntity<EmailExists> emailExists(@RequestParam("email") String email,@AuthenticationPrincipal SecurityDetails details){
        return ResponseEntity.ok(service.emailExists(email, details));
    }
    @GetMapping("/share/project/{id}")
    public ResponseEntity<String> shareProject(@PathVariable("id") Long projectId){
        return ResponseEntity.ok(service.shareProject(projectId));
    }
    @GetMapping("/projects")
    public ResponseEntity<List<ProjectResponseDto>> getOrganizationProjects(@AuthenticationPrincipal SecurityDetails details){
        return ResponseEntity.ok(mapper.toDtos(service.getProjects(details)));
    }

    @GetMapping("/projects/participants")
    public ResponseEntity<List<ParticipationDto>> getProjectParticipants(@AuthenticationPrincipal SecurityDetails details){
        return ResponseEntity.ok(service.getProjectParticipants(details));
    }
    @PatchMapping("/projects/participant")
    public ResponseEntity<Void> updateParticipation(@RequestBody UpdateParticipantDto payload){
        service.updateVolunteerParticipation(payload);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/projects/broadcast")
    public ResponseEntity<Void> createBroadcastMessageToParticipants(@AuthenticationPrincipal SecurityDetails details, @RequestBody BroadcastMsgDto payload)  {
        service.sendBroadcast(details, payload.projectId(), payload.message());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/projects")
    public ResponseEntity<List<ProjectResponseDto>> createProject(@RequestBody @Valid ProjectRequestDto projectRequestDto, @AuthenticationPrincipal SecurityDetails details){
        return ResponseEntity.ok(service.createProject(projectRequestDto, details));
    }

    @PatchMapping("/projects/{projectId}")
    public ResponseEntity<ProjectResponseDto> updateProject(@PathVariable("projectId") Long projectId, @RequestBody ProjectRequestDto projectRequestDto){
        return ResponseEntity.accepted().body(service.updateProject(projectId, projectRequestDto));
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

    @GetMapping("/projects/applicants")
    public ResponseEntity<List<VolunteerApplicationDto>> getProjectApplication(@AuthenticationPrincipal SecurityDetails details){
        return ResponseEntity.ok(service.getProjectApplications(details));
    }

    @PatchMapping("/projects/application/{id}/accept")
    public ResponseEntity<Void> acceptApplication(@AuthenticationPrincipal SecurityDetails details, @PathVariable("id") Long id){
        service.approveApplication(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/projects/application/{id}/reject")
    public ResponseEntity<Void> rejectApplication(@PathVariable("id") Long id){
        service.rejectApplication(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/projects/{projectId}/publish")
    public ResponseEntity<Void> publicProject(@PathVariable("projectId") Long id, @AuthenticationPrincipal SecurityDetails details){

        service.publishProject(id, details);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/projects/{projectId}")
    public ResponseEntity<Void> deleteProject(@PathVariable("projectId") Long projectId, @AuthenticationPrincipal SecurityDetails details){
        service.deleteProject(projectId, details.getId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/otp/request")
    public ResponseEntity<Void> requestOtp(@AuthenticationPrincipal SecurityDetails details, @RequestParam("purpose") OtpPurpose purpose){
        service.requestOtp( details.getUsername(),purpose);
        return ResponseEntity.accepted().build();
    }

    @PatchMapping("/verify/email")
    public ResponseEntity<Void> confirmEmail(@RequestBody @Valid OtpDto otpDto, @AuthenticationPrincipal SecurityDetails details){
        service.confirmEmail(details, otpDto.otp());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/password/update")
    public ResponseEntity<Void> updatePassword(@RequestBody @Valid PasswordUpdateDto passwordUpdateDto, @AuthenticationPrincipal SecurityDetails details){
        service.updatePassword(passwordUpdateDto, details);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logout")
    ResponseEntity<Void> logout(@AuthenticationPrincipal SecurityDetails authUser) {
        return ResponseEntity.ok().headers(logoutService.logout(authUser)).build();
    }
}
