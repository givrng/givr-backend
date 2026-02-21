package com.backend.givr.shared.controller;

import com.backend.givr.organization.service.OrganizationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Formatter;

@RequestMapping("/v1/api/webhook/monnify")
public class MonnifyController {
    private static final String HMACSHA532 = "HmacSHA512";

    @Value("${monnify.secret.key}")
    private String SECRETKEY;

    @Autowired
    private OrganizationService organizationService;

    private static String toHexString(byte[] bytes) {
        Formatter formatter = new Formatter();
        for (byte b : bytes) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }

    public String calculateHMAC512TransactionHash(String data, String merchantClientSecret) throws SignatureException, NoSuchAlgorithmException, InvalidKeyException {
        SecretKeySpec secretKeySpec = new SecretKeySpec(merchantClientSecret.getBytes(), HMACSHA532);
        Mac mac = Mac.getInstance(HMACSHA532);
        mac.init(secretKeySpec);

        return toHexString(mac.doFinal(data.getBytes()));
    }


    @PostMapping
    @Async
    public ResponseEntity<Void> confirmPayment(@RequestBody String payload, @RequestHeader("monnify-signature") String signature) throws SignatureException, NoSuchAlgorithmException, InvalidKeyException, JsonProcessingException {
        if(!signature.equals(calculateHMAC512TransactionHash(payload, SECRETKEY)))
            return ResponseEntity.badRequest().build();
        System.out.println(payload);

        return ResponseEntity.ok().build();
    }
}
