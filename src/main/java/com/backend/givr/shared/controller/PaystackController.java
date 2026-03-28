package com.backend.givr.shared.controller;

import com.backend.givr.shared.service.PaystackClient;
import com.backend.givr.shared.service.VerificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Hidden
@RestController
@RequestMapping("/v1/api/webhook/paystack")
public class PaystackController {

    @Autowired
    private PaystackClient merchant;
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private VerificationService verificationService;

    @PostMapping
    public ResponseEntity<Void> handleNotification(@RequestBody String body, @RequestHeader("x-paystack-signature") String signature){
        if(merchant.hashIsValid(body, signature)){
            verificationService.initiateVerification(body);
            return ResponseEntity.ok().build();
        }
        return null;
    }
}
