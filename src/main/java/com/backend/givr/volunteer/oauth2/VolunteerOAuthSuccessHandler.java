package com.backend.givr.volunteer.oauth2;

import com.backend.givr.shared.jwt.GivrCookie;
import com.backend.givr.shared.enums.AuthProviderType;
import com.backend.givr.volunteer.security.VolunteerDetails;
import com.backend.givr.volunteer.security.VolunteerDetailsService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;

@RequiredArgsConstructor
public class VolunteerOAuthSuccessHandler implements AuthenticationSuccessHandler {
    private final VolunteerDetailsService service;
    private final String appBaseUrl;
    private final GivrCookie givrCookie;
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OidcUser authUser = (OidcUser) authentication.getPrincipal();
        assert authUser != null;

        VolunteerDetails details = service.loadUserByUsername(authUser.getEmail());
        Object redirect = request.getSession().getAttribute("REDIRECT_AFTER_LOGIN");

        if(details.getAuthProvider() != AuthProviderType.GOOGLE){
            response.sendRedirect(String.format("%s/signin/volunteer?error=Account was registered with different sign method. Input username & password instead", appBaseUrl));
            return;
        }
        givrCookie.addCookieToResponse(details, response);
        if(redirect != null)
            response.sendRedirect(String.format("%s/volunteer?%s", appBaseUrl, redirect));
        else
            response.sendRedirect(String.format("%s/volunteer", appBaseUrl));
    }
}
