package com.backend.givr.admin.controller;

import com.backend.givr.admin.dtos.AdminAuthDto;
import com.backend.givr.admin.dtos.ReviewDto;
import com.backend.givr.admin.dtos.ReviewResponseDto;
import com.backend.givr.admin.entity.AdminDetails;
import com.backend.givr.admin.service.AdminService;
import com.backend.givr.shared.dtos.VerificationSessionDto;
import com.backend.givr.shared.entity.OrganizationVerificationSession;
import com.backend.givr.shared.enums.ReviewStatus;
import com.backend.givr.shared.interfaces.SecurityDetails;
import com.backend.givr.shared.jwt.GivrCookie;
import jakarta.annotation.security.RolesAllowed;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/${api.version}/api/admin")
public class AdminController {

    @Autowired
    private AdminService service;
    @Autowired
    private GivrCookie givrCookie;

    @Value("${givr.allowed.admins}")
    private List<String> allowedAdmins;

    @GetMapping
    public ResponseEntity<List<VerificationSessionDto>> getVerificationSession(@RequestParam(value = "status", required = false) ReviewStatus status){
        return ResponseEntity.ok(service.getVerificationSession(status));
    }

    @PostMapping("/auth/request-otp")
    public ResponseEntity<Void> requestOtp(@RequestBody AdminAuthDto payload){
        if(allowedAdmins.contains(payload.email().toLowerCase())) {

            // Checks that an admin account doesn't exist before creating
            if(!service.adminExists(payload.email()))
                service.createAdmin(payload.email());

            service.requestOtp(payload.email());
        }
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/auth/verify-otp")
    public ResponseEntity<Void> authenticateAdmin(@RequestBody AdminAuthDto payload, HttpServletResponse response) throws IOException {
        AdminDetails user = service.validateOtp(payload);
        givrCookie.addCookieToResponse(user, response);
        return ResponseEntity.ok().build();
    }



    @GetMapping("/verify")
    public ResponseEntity<Void> verify(){
        return ResponseEntity.ok().build();
    }
    @PostMapping("/review")
    public ResponseEntity<ReviewResponseDto> createReview(@RequestBody ReviewDto reviewDto, @AuthenticationPrincipal SecurityDetails admin){
        return ResponseEntity.accepted().body(service.createReview(reviewDto, admin));
    }


    @GetMapping("/review/{sessionId}")
    public ResponseEntity<ReviewResponseDto> getLastSessionReview(@PathVariable(name = "sessionId") Long sessionId){
        return ResponseEntity.ok(service.getLastSessionReview(sessionId));
    }

    @DeleteMapping
    @RolesAllowed({"SUPER_ADMIN"})
    public ResponseEntity<Void> removeAdmin(){
        return null;
    }
}
