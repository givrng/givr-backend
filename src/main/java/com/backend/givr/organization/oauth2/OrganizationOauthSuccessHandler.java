package com.backend.givr.organization.oauth2;

import com.backend.givr.organization.security.OrganizationDetails;
import com.backend.givr.organization.security.OrganizationDetailsService;
import com.backend.givr.shared.jwt.GivrCookie;
import com.backend.givr.shared.enums.AuthProviderType;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;
@RequiredArgsConstructor
public class OrganizationOauthSuccessHandler implements AuthenticationSuccessHandler {
    private final OrganizationDetailsService service;
    private final GivrCookie givrCookie;
    private final String appBaseUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OidcUser oidcUser = (OidcUser) authentication.getPrincipal();
        assert oidcUser != null;
        OrganizationDetails organizationDetails = service.loadUserByUsername(oidcUser.getEmail());

        if(organizationDetails.getAuthProvider() != AuthProviderType.GOOGLE){
            response.sendRedirect(String.format("%s/signin/organization?error=Account was registered with a different sign in method, sign in username & password instead", appBaseUrl));
            return;
        }

        givrCookie.addCookieToResponse(organizationDetails, response);
        response.sendRedirect(String.format("%s/organization", appBaseUrl));
    }
}
