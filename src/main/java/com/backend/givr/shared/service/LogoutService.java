package com.backend.givr.shared.service;

import com.backend.givr.shared.interfaces.SecurityDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.server.Cookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class LogoutService {
    @Value("${api.version}")
    private String apiVersion;

    @Autowired
    private TokenIdService service;

    public HttpHeaders logout(@AuthenticationPrincipal SecurityDetails authUser) {

        HttpHeaders headers = new HttpHeaders();
        service.revokeTokens(authUser.getUsername());

        headers.add(HttpHeaders.SET_COOKIE, ResponseCookie.from("RefreshToken")
                .sameSite(Cookie.SameSite.LAX.attributeValue())
                .path("/")
                .httpOnly(true)
                .secure(true)
                .maxAge(Duration.ZERO)
                .build().toString());

        headers.add(HttpHeaders.SET_COOKIE, ResponseCookie.from("AccessToken")
                .sameSite(Cookie.SameSite.LAX.attributeValue())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(Duration.ZERO)
                .build().toString());

        return headers;
    }
}
