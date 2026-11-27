package com.backend.givr.shared.controller;

import com.backend.givr.config.properties.VerificationFeeProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/${api.version}/api/public")
@RequiredArgsConstructor
public class PublicController {
    private final VerificationFeeProperties feeProperties;

    @GetMapping("/payments/fees")
    public ResponseEntity<VerificationFeeProperties> getPaymentFees(){
        return ResponseEntity.ok(feeProperties);
    }
}
