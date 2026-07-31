package com.backend.givr.shared.jwt;

import com.backend.givr.shared.AuthDTO;
import com.backend.givr.shared.interfaces.SecurityDetails;
import com.backend.givr.shared.service.TokenIdService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.web.server.Cookie;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private final JwtUtil jwtUtil;
    private final ObjectMapper mapper = new ObjectMapper();
    private final TokenIdService tokenService;
    private final AuthenticationManager authManager;

    //private final CreatorService creatorService;
    public JwtAuthenticationFilter(JwtUtil service, AuthenticationManager authManager, TokenIdService tokenService){
        this.jwtUtil = service;
        this.authManager = authManager;
        this.tokenService = tokenService;
    }
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        try{
            var auth = mapper.readValue(request.getInputStream(), AuthDTO.class);
            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(auth.email(), auth.password());
            return authManager.authenticate(token);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }catch (AuthenticationException e){
            throw new BadCredentialsException("Invalid credentials", e);
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        String accessId = JwtUtil.generateJti();
        String accessToken = jwtUtil.generateToken((SecurityDetails) authResult.getPrincipal(), accessId, JwtUtil.ACCESSEXPIRATION.toMillis());
        String refreshId = JwtUtil.generateJti();
        String refreshToken= jwtUtil.generateToken((SecurityDetails) authResult.getPrincipal(), refreshId, JwtUtil.REFRESHEXPIRATION.toMillis());

        String email = ((UserDetails) authResult.getPrincipal()).getUsername();

        tokenService.createToken(refreshId, email, JwtUtil.REFRESHEXPIRATION.toMillis());

        ResponseCookie accessCookie = ResponseCookie.from("AccessToken").value(accessToken)
                                        .path("/")
                                        .maxAge(JwtUtil.ACCESSEXPIRATION)
                                        .sameSite(Cookie.SameSite.NONE.attributeValue())
                                        .httpOnly(true)
                                        .secure(true)
                                        .build();

        ResponseCookie refreshCookie = ResponseCookie.from("RefreshToken").value(refreshToken)
                                        .path("/auth")
                                        .maxAge(JwtUtil.REFRESHEXPIRATION)
                                        .sameSite(Cookie.SameSite.NONE.attributeValue())
                                        .httpOnly(true)
                                        .secure(true)
                                        .build();

        response.addHeader("Set-Cookie", accessCookie.toString());
        response.addHeader("Set-Cookie", refreshCookie.toString());

        response.setStatus(200);
        response.getWriter().flush();
    }
}
