package com.backend.givr.volunteer.controllers;

import com.backend.givr.organization.dtos.UpdateParticipantDto;
import com.backend.givr.shared.dtos.*;
import com.backend.givr.shared.enums.DonationStatus;
import com.backend.givr.shared.enums.MediaUploadPurpose;
import com.backend.givr.shared.enums.ProjectStatus;
import com.backend.givr.shared.interfaces.SecurityDetails;
import com.backend.givr.shared.service.CloudinaryService;
import com.backend.givr.shared.service.InitiativeService;
import com.backend.givr.volunteer.service.IndividualService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/${api.version}/api/individual")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('INDIVIDUAL')")
public class IndividualController {
    private final IndividualService service;
    private final CloudinaryService cloudinaryService;
    private final InitiativeService initiativeService;
    private final ObjectMapper objectMapper;
    // Individual
    @PostMapping("/initiative")
    ResponseEntity<List<InitiativeResponseDto>> createInitiative(@RequestBody InitiativeRequestDto payload, @AuthenticationPrincipal SecurityDetails authContext){
        return ResponseEntity.ok(service.createInitiative(payload, authContext));
    }

    @GetMapping("/initiative")
    public ResponseEntity<PagedModel<InitiativeResponseDto>> getInitiatives(
            @RequestParam(name = "status", defaultValue = "DRAFT") ProjectStatus status,
            @RequestParam(name = "pageNum", defaultValue = "0") int pageNum,
            @RequestParam(name = "pageSize", defaultValue = "20") int pageSize,
            @AuthenticationPrincipal SecurityDetails details){
        return ResponseEntity.ok(service.getInitiatives(status, pageNum, pageSize, details.getId()));
    }


    @GetMapping("/donations")
    public ResponseEntity<PagedModel<DonationDto>> getIndividualDonations(@AuthenticationPrincipal SecurityDetails details,
                                                                          @RequestParam(name = "pageNum", defaultValue = "0") int pageNum,
                                                                          @RequestParam(name = "status", required = false) DonationStatus status,
                                                                          @RequestParam(name = "pageSize", defaultValue = "20") int pageSize){
        PageRequest request = PageRequest.of(pageNum, pageSize);
        return ResponseEntity.ok(service.getPersonalDonations(details.getId(), status, request));
    }

    @GetMapping("/media/upload/signature")
    public ResponseEntity<MediaUploadResponse> generateUploadSignature(@RequestParam(name = "purpose") MediaUploadPurpose purpose){
        return ResponseEntity.ok(cloudinaryService.generateSignedUploadKey(purpose));
    }

    @PatchMapping("/initiative/publish")
    public ResponseEntity<Void> publishProject(@RequestBody @Valid PublishProjectDto publishProjectDto, @AuthenticationPrincipal SecurityDetails details){
        var checkout = initiativeService.initializePublishProject(publishProjectDto, details);
        if(checkout == null)
            return ResponseEntity.accepted().build();
        return ResponseEntity.created(URI.create(checkout.checkoutUrl())).build();
    }

    @GetMapping("/initiative/participants")
    public ResponseEntity<List<ParticipationDto>> getProjectParticipants(@AuthenticationPrincipal SecurityDetails details){
        return ResponseEntity.ok(service.getProjectParticipants(details));
    }

    @PatchMapping("/initiative/participant")
    public ResponseEntity<Void> updateParticipation(@RequestBody UpdateParticipantDto payload, @AuthenticationPrincipal SecurityDetails details){
        service.updateVolunteerParticipation(payload, details.getId());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/initiative/{initiativeId}")
    public ResponseEntity<InitiativeResponseDto> updateProject(@PathVariable("initiativeId") Long projectId, @RequestBody InitiativeRequestDto projectRequestDto, @AuthenticationPrincipal SecurityDetails details) throws JsonProcessingException {
        return ResponseEntity.accepted().body(service.updateProject(projectId, projectRequestDto, details.getId()));
    }

    @PatchMapping("/initiative/{initiative}/completed")
    public ResponseEntity<Void> markProjectCompleted(@PathVariable("initiative") Long projectId, @AuthenticationPrincipal SecurityDetails details){
        service.markInitiativeComplete(projectId, details.getId());
        return ResponseEntity.accepted().build();
    }


    @GetMapping("/initiative/applications")
    public ResponseEntity<PagedModel<VolunteerApplicationDto>> getProjectApplication(@AuthenticationPrincipal SecurityDetails details,
                                                                               @RequestParam(name = "pageNum", defaultValue = "0") int pageNum,
                                                                               @RequestParam(name = "pageSize", defaultValue = "30") int pageSize){
        PageRequest request = PageRequest.of(pageNum, pageSize);
        return ResponseEntity.ok(service.getProjectApplications(details, request));
    }

    @GetMapping("/initiative/donations")
    public ResponseEntity<PagedModel<DonationDto>> getAllInitiativeDonations(@AuthenticationPrincipal SecurityDetails details,
                                                                                    @RequestParam(name = "status", required = false) DonationStatus status,
                                                                                  @RequestParam(name = "pageNum", defaultValue = "0") int pageNum,
                                                                                  @RequestParam(name = "pageSize", defaultValue = "30") int pageSize){
        PageRequest request = PageRequest.of(pageNum, pageSize);
        return ResponseEntity.ok(service.getDonations(details.getId(), status, request));
    }

    @GetMapping("/initiative/donations/{donationId}")
    public ResponseEntity<DonationDto> getInitiativeDonation(@PathVariable(name = "donationId") String donationId){
        return ResponseEntity.ok(service.getDonation(donationId));
    }

    @GetMapping("/initiative/{initiativeId}/donations")
    public ResponseEntity<List<InitiativeItemDto>> getDonationsMadeToInitiative(@PathVariable(name = "initiativeId") Long initiativeId,
                                                                                @AuthenticationPrincipal SecurityDetails details){
        return ResponseEntity.ok(service.getInitiativeDonations(initiativeId, details));
    }

    @PatchMapping("/initiative/donations/{donationId}")
    public ResponseEntity<DonationDto> updateDonationStatus(@Valid @RequestBody DonationUpdateDto donationUpdateDto, @AuthenticationPrincipal SecurityDetails details){
        return ResponseEntity.accepted().body(service.updateDonationStatus(donationUpdateDto, details.getId()));
    }

    @PatchMapping("/initiative/applications/{id}/accept")
    public ResponseEntity<Void> acceptApplication(@AuthenticationPrincipal SecurityDetails details, @PathVariable("id") Long id){
        service.approveApplication(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/initiative/applications/{id}/reject")
    public ResponseEntity<Void> rejectApplication(@PathVariable("id") Long id){
        service.rejectApplication(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/initiative/{initiativeId}")
    public ResponseEntity<Void> deleteProject(@PathVariable("initiativeId") Long projectId, @AuthenticationPrincipal SecurityDetails details){
        service.deleteInitiative(projectId, details.getId());
        return ResponseEntity.noContent().build();
    }
}
