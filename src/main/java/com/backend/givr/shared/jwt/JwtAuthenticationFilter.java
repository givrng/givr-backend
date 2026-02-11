package com.backend.givr.shared.jwt;

import com.backend.givr.shared.dtos.AuthDTO;
import com.backend.givr.shared.interfaces.SecurityDetails;
import com.backend.givr.shared.service.TokenIdService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final ObjectMapper mapper = new ObjectMapper();
    private final TokenIdService tokenService;
    private final AuthenticationProvider authManager;
    private final GivrCookie givrCookie;

    //private final CreatorService creatorService;
    public JwtAuthenticationFilter(GivrCookie givrCookie, AuthenticationProvider provider, TokenIdService tokenService){
        this.givrCookie = givrCookie;
        this.authManager = provider;
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
        SecurityDetails details = (SecurityDetails) authResult.getPrincipal();

        givrCookie.addCookieToResponse(details, response);
        response.setStatus(200);
        response.getWriter().flush();
    }
}
