package com.backend.givr.shared.jwt;


import com.backend.givr.shared.interfaces.SecurityDetails;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class JwtUtil {
    @Value("${jwt.secret}")
    private String token;
    SecretKey secretKey;
    public static Duration ACCESSEXPIRATION = Duration.ofMinutes(60);// 15 minutes
    public static Duration REFRESHEXPIRATION = Duration.ofDays(1); // 24 hours
    public static String generateJti(){
        return UUID.randomUUID().toString();
    }

    @PostConstruct
    public void setSecretKey(){
        secretKey = Keys.hmacShaKeyFor(token.getBytes(StandardCharsets.UTF_8));
    }
    public String generateToken(SecurityDetails user, String id, long jwtExpiration) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getAuthorities().stream().toList().getFirst().getAuthority());
        claims.put("userId", user.getId());
        return Jwts.builder()
                .subject(user.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .claims(claims)
                .id(id)
                .signWith(secretKey)
                .compact();
    }

    public String extractUsername(String token){
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
    public String extractRoles(String token) throws JsonProcessingException {
        String role = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("role", String.class);

        return role;
    }

    public String extractUserId(String token){
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("userId", String.class);
    }
    public String extractTokenId(String token){
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getId();
    }
    public boolean isTokenExpired(String token){
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration()
                .before(new Date(System.currentTimeMillis()));
    }

    public boolean isTokenValid(String token, UserDetails user){
        String username = extractUsername(token);
        return username.equals(user.getUsername()) && !isTokenExpired(token);
    }

}
