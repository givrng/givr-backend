package com.backend.givr.shared.controller;

import com.backend.givr.shared.dtos.VolunteerCertificateVerificationDTO;
import com.backend.givr.shared.service.Certificate.CertificateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/${api.version}/api/certificates")
public class CertificateVerificationController {
    @Autowired
    private CertificateService service;

    @GetMapping("/verify/{certId}")
    public ResponseEntity<VolunteerCertificateVerificationDTO> verifyCertificate(@PathVariable String certId){
        return ResponseEntity.ok(service.verifyCertificate(certId));
    }
}
