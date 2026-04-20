package com.backend.givr.volunteer.oauth2;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
/**
 * Captures redirect parameter if present*/
public class VolunteerOAuthFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String redirect = request.getParameter("redirect");

        if(redirect != null){
            request.getSession().setAttribute("REDIRECT_AFTER_LOGIN", redirect);
        }

        filterChain.doFilter(request, response);
    }
}
