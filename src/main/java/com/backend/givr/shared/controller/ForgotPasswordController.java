package com.backend.givr.shared.controller;

import com.backend.givr.shared.dtos.OtpRequestDto;
import com.backend.givr.shared.dtos.PasswordResetDto;
import com.backend.givr.shared.service.PasswordResetService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/${api.version}/api/password")
public class ForgotPasswordController {

    @Autowired
    private PasswordResetService service;

    @PostMapping("/forgot")
    public ResponseEntity<Void> requestPasswordReset(@RequestBody @Valid OtpRequestDto emailDto){
        service.requestPasswordReset(emailDto.email(), emailDto.role());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reset")
    public ResponseEntity<Void> resetPassword(@RequestBody @Valid PasswordResetDto passwordResetDto){
        service.resetPassword(passwordResetDto.email(), passwordResetDto.role(), passwordResetDto.newPassword(), passwordResetDto.otp());
        return ResponseEntity.noContent().build();
    }
}
