package com.backend.givr.shared.jwt;

import com.backend.givr.organization.security.OrganizationDetailsService;
import com.backend.givr.shared.interfaces.SecurityDetails;
import com.backend.givr.volunteer.security.VolunteerDetailsService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.stream.Collectors;

public class JwtValidationFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final VolunteerDetailsService volunteerDetailsService;
    private final OrganizationDetailsService organizationDetailsService;
    public JwtValidationFilter(JwtUtil util, VolunteerDetailsService volunteerDetailsService, OrganizationDetailsService organizationDetailsService){
        jwtUtil =util;
        this.volunteerDetailsService = volunteerDetailsService;
        this.organizationDetailsService = organizationDetailsService;

    }
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if(request.getCookies()== null){
            filterChain.doFilter(request, response);
            return;
        }
        try{
            Cookie[] cookies = request.getCookies();
            String accessToken = null;
            for(Cookie cookie: cookies){
                if(cookie.getName().equals("AccessToken")){
                    accessToken = cookie.getValue();
                    break;
                }
            }
            if(StringUtils.hasText(accessToken)) {
                String role = jwtUtil.extractRoles(accessToken);
                SecurityDetails user;
                if(role.equals("VOLUNTEER"))
                    user = volunteerDetailsService.loadUserByUsername(jwtUtil.extractUsername(accessToken));
                else
                    user = organizationDetailsService.loadUserByUsername(jwtUtil.extractUsername(accessToken));

                if (jwtUtil.isTokenValid(accessToken, user)) {

                    UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(token);
                }
            }
            filterChain.doFilter(request, response);
        } catch (JwtException e){
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Expired or Invalid token");
        }

    }
}
