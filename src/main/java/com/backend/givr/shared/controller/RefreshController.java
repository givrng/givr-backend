package com.backend.givr.shared.controller;

import com.backend.givr.organization.entity.Organization;
import com.backend.givr.organization.security.OrganizationDetails;
import com.backend.givr.shared.interfaces.SecurityDetails;
import com.backend.givr.shared.jwt.JwtUtil;
import com.backend.givr.shared.service.TokenIdService;
import com.backend.givr.volunteer.entity.Volunteer;
import com.backend.givr.volunteer.security.VolunteerDetails;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.Duration;
import java.util.Objects;

@RestController
@RequestMapping("/${api.version}/api/auth")
public class RefreshController {
    private final TokenIdService service;
    private final JwtUtil util;

    @Value("${api.version}")
    private String apiVersion;

    @PersistenceContext
    private EntityManager manager;

    RefreshController(TokenIdService tokenService, JwtUtil util){
        service = tokenService;
        this.util = util;
    }
    @GetMapping("/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest request){
        String refreshToken= null;

        if(request.getCookies() == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        for(Cookie cookie: request.getCookies()){
            if(cookie.getName().equals("RefreshToken")) {
                refreshToken = cookie.getValue();
                break;
            }
        }

        if(!StringUtils.hasText(refreshToken))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        if(service.isTokenValid(util.extractTokenId(refreshToken))){// validates, revokes, and set used
            String username;
            String userId;
            String authority;
            try{
                authority = util.extractRoles(refreshToken);
                username = util.extractUsername(refreshToken);
                userId = util.extractUserId(refreshToken);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            SecurityDetails userDetails;
            if(Objects.equals(authority, "VOLUNTEER")){
                Volunteer volunteer = manager.getReference(Volunteer.class, userId);
                userDetails = new VolunteerDetails(username, null, volunteer);
                userDetails.setAuthorities();
            }else{
                Organization organization = manager.getReference(Organization.class, userId);
                userDetails = new OrganizationDetails(username, null, organization);
                userDetails.setAuthorities();
            }

            String refreshId = JwtUtil.generateJti();
            String accessId = JwtUtil.generateJti();

            String accessToken= util.generateToken(userDetails, accessId, JwtUtil.ACCESSEXPIRATION.toMillis());
            refreshToken = util.generateToken(userDetails, refreshId, JwtUtil.REFRESHEXPIRATION.toMillis());

            service.createToken(refreshId, username, JwtUtil.REFRESHEXPIRATION.toMillis()); // saves token to db

            ResponseCookie accessCookie = ResponseCookie.from("AccessToken").value(accessToken)
                    .maxAge(JwtUtil.ACCESSEXPIRATION)
                    .path("/")
                    .secure(true)
                    .httpOnly(true)
                    .sameSite("None")
                    .build();
            ResponseCookie refreshCookie = ResponseCookie.from("RefreshToken").value(refreshToken)
                    .maxAge(JwtUtil.REFRESHEXPIRATION)
                    .path(String.format("/%s/api/auth", apiVersion))
                    .secure(true)
                    .httpOnly(true)
                    .sameSite("None")
                    .build();
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.SET_COOKIE, accessCookie.toString());
            headers.add(HttpHeaders.SET_COOKIE, refreshCookie.toString());
            return ResponseEntity.ok().headers(headers).build();
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}