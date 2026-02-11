package com.backend.givr.organization.oauth2;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import java.io.IOException;

@RequiredArgsConstructor
public class OrganizationOAuthFailureHandler implements AuthenticationFailureHandler {

    private final String appBaseUrl;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        response.sendRedirect(String.format("%s/signin/organization?error=Unexpected error, try username/password to sign in", appBaseUrl));
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().flush();
    }
}
