package com.backend.givr.volunteer.controllers;

import com.backend.givr.organization.dtos.ProjectRequestDto;
import com.backend.givr.organization.dtos.UpdateParticipantDto;
import com.backend.givr.shared.dtos.InitiativeResponseDto;
import com.backend.givr.shared.dtos.ParticipationDto;
import com.backend.givr.shared.dtos.VolunteerApplicationDto;
import com.backend.givr.shared.interfaces.SecurityDetails;
import com.backend.givr.shared.service.ParticipationService;
import com.backend.givr.volunteer.service.IndividualService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/${api.version}/api/individual")
@RequiredArgsConstructor
public class IndividualController {
    private final IndividualService service;
    private final ParticipationService participationService;

    // Initiative
    @PostMapping("/initiative")
    ResponseEntity<List<InitiativeResponseDto>> createInitiative(@RequestBody ProjectRequestDto payload, @AuthenticationPrincipal SecurityDetails authContext){
        return ResponseEntity.ok(service.createInitiative(payload, authContext));
    }

    @PatchMapping("/initiative/{initiativeId}/publish")
    public ResponseEntity<Void> publicProject(@PathVariable("initiativeId") Long id, @AuthenticationPrincipal SecurityDetails details){

        service.publishProject(id, details);
        return ResponseEntity.noContent().build();
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
    public ResponseEntity<InitiativeResponseDto> updateProject(@PathVariable("projectId") Long projectId, @RequestBody ProjectRequestDto projectRequestDto, @AuthenticationPrincipal SecurityDetails details){
        return ResponseEntity.accepted().body(service.updateProject(projectId, projectRequestDto, details.getId()));
    }

    @PatchMapping("/initiative/{initiative}/completed")
    public ResponseEntity<Void> markProjectCompleted(@PathVariable("initiative") Long projectId){
        participationService.markProjectCompleted(projectId);
        return ResponseEntity.accepted().build();
    }


    @GetMapping("/initiative/applicants")
    public ResponseEntity<List<VolunteerApplicationDto>> getProjectApplication(@AuthenticationPrincipal SecurityDetails details){
        return ResponseEntity.ok(service.getProjectApplications(details));
    }

    @PatchMapping("/initiative/application/{id}/accept")
    public ResponseEntity<Void> acceptApplication(@AuthenticationPrincipal SecurityDetails details, @PathVariable("id") Long id){
        service.approveApplication(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/projects/application/{id}/reject")
    public ResponseEntity<Void> rejectApplication(@PathVariable("id") Long id){
        service.rejectApplication(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/initiative/{initiativeId}")
    private ResponseEntity<Void> deleteProject(@PathVariable("projectId") Long projectId, @AuthenticationPrincipal SecurityDetails details){
        service.deleteProject(projectId, details.getId());
        return ResponseEntity.noContent().build();
    }
}
