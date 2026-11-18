package com.backend.givr.shared.jwt;

import com.backend.givr.admin.service.AdminDetailsService;
import com.backend.givr.organization.security.OrganizationDetailsService;
import com.backend.givr.shared.exceptions.InvalidTokenException;
import com.backend.givr.shared.interfaces.SecurityDetails;
import com.backend.givr.volunteer.security.VolunteerDetailsService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
@Slf4j
public class JwtValidationFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final VolunteerDetailsService volunteerDetailsService;
    private final OrganizationDetailsService organizationDetailsService;
    private final AdminDetailsService adminDetailsService;
    private final List<String> ignorePaths = List.of("/v1/api/volunteer/auth/",
            "/v1/api/volunteer/oauth2/", "/v1/api/organization/auth/", "/v1/api/organization/oauth2/");
    public JwtValidationFilter(JwtUtil util, VolunteerDetailsService volunteerDetailsService, OrganizationDetailsService organizationDetailsService, AdminDetailsService adminDetailsService){
        jwtUtil =util;
        this.volunteerDetailsService = volunteerDetailsService;
        this.organizationDetailsService = organizationDetailsService;
        this.adminDetailsService = adminDetailsService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        for(String path: ignorePaths){
            if(request.getServletPath().contains(path)) {
                System.out.println("Ignored, "+request.getServletPath());
                return true;
            }
        }
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if(request.getCookies()== null || request.getServletPath().endsWith("/auth/login")){
            filterChain.doFilter(request, response);
            return;
        }
        try{
            Cookie[] cookies = request.getCookies();

            if (cookies == null) {
                log.error("Cookies were not found");
                filterChain.doFilter(request, response);
                return;
            }

            String accessToken = null;
            for(Cookie cookie: cookies){
                if(cookie.getName().equals("AccessToken")){
                    accessToken = cookie.getValue();
                    log.info("Access token found {}", accessToken);
                    break;
                }
            }
            if(StringUtils.hasText(accessToken)) {
                String[] roles = jwtUtil.extractRoles(accessToken);
                assert roles != null;

                log.info("User with {} roles attempting validation", roles.length);
                SecurityDetails user;
                if(Arrays.stream(roles).toList().contains("VOLUNTEER"))
                    user = volunteerDetailsService.loadUserByUsername(jwtUtil.extractUsername(accessToken));
                else if (Arrays.stream(roles).toList().contains("ORGANIZATION"))
                    user = organizationDetailsService.loadUserByUsername(jwtUtil.extractUsername(accessToken));
                else{
                    user = adminDetailsService.loadUserByUsername(jwtUtil.extractUsername(accessToken));
                }

                if (jwtUtil.isTokenValid(accessToken, user)) {
                    UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(token);
                    log.info("User {} has validated successfully", user.getId());
                }else{
                    log.error("Failed to validate user, invalid token");
                    throw new InvalidTokenException("Invalid token");
                }
            }else{
                filterChain.doFilter(request, response);
                return;
            }

        } catch (JwtException | InvalidTokenException e){
            log.trace("Validation failed", e.fillInStackTrace());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getLocalizedMessage());
            return;
        }
        filterChain.doFilter(request, response);
    }
}
