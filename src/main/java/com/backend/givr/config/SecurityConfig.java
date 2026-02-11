package com.backend.givr.config;

import com.backend.givr.organization.security.OrganizationDetailsService;
import com.backend.givr.shared.jwt.JwtAuthenticationFilter;
import com.backend.givr.shared.jwt.JwtUtil;
import com.backend.givr.shared.jwt.JwtValidationFilter;
import com.backend.givr.shared.service.TokenIdService;
import com.backend.givr.volunteer.security.VolunteerDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@EnableWebSecurity
@Configuration
public class SecurityConfig {

    @Autowired
    private VolunteerDetailsService volunteerDetailsService;
    @Autowired
    private OrganizationDetailsService organizationDetailsService;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private TokenIdService tokenIdService;

    @Bean
    PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    AuthenticationManager volunteerAuthManager (VolunteerDetailsService detailsService){
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(detailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(provider);
    }

    AuthenticationManager organizationAuthManager (OrganizationDetailsService detailsService){
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(detailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(provider);
    }

    @Bean
    @Order(0)
    SecurityFilterChain volunteerSecurityFilter(HttpSecurity httpSecurity) throws Exception {
        JwtAuthenticationFilter authFilter = new JwtAuthenticationFilter(jwtUtil, volunteerAuthManager(volunteerDetailsService), tokenIdService);
        authFilter.setFilterProcessesUrl("/v1/api/volunteer/auth/login");
        JwtValidationFilter validationFilter = new JwtValidationFilter(jwtUtil, volunteerDetailsService, organizationDetailsService);

        return httpSecurity
            .securityMatcher("/v1/api/volunteer/**")
            .cors(Customizer.withDefaults())
            .authorizeHttpRequests(request->{
            request.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();
            request.requestMatchers(HttpMethod.POST, "/v1/api/volunteer/auth/**").permitAll();
            request.anyRequest().hasAuthority("VOLUNTEER");
        })
                .authenticationManager(volunteerAuthManager(volunteerDetailsService))
                .addFilter(authFilter)
                .addFilterBefore(validationFilter, UsernamePasswordAuthenticationFilter.class)
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .build();
    }
    @Bean
    @Order(1)
    SecurityFilterChain organizationSecurityFilter(HttpSecurity httpSecurity) throws Exception {
        JwtAuthenticationFilter authFilter = new JwtAuthenticationFilter(jwtUtil, organizationAuthManager(organizationDetailsService), tokenIdService);
        authFilter.setFilterProcessesUrl("/v1/api/organization/auth/login");
        JwtValidationFilter validationFilter = new JwtValidationFilter(jwtUtil, volunteerDetailsService, organizationDetailsService);

        return httpSecurity
                .securityMatcher("/v1/api/organization/**")
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(request->{
                    request.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();
                    request.requestMatchers(HttpMethod.POST, "/v1/api/organization/auth/**").permitAll();
                    request.anyRequest().hasAuthority("ORGANIZATION");
                })
                .authenticationManager(organizationAuthManager(organizationDetailsService))
                .addFilter(authFilter)
                .addFilterBefore(validationFilter, UsernamePasswordAuthenticationFilter.class)
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .build();
    }

    @Bean
    @Order(2)
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .securityMatcher("/**")
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth->auth.anyRequest().permitAll())
                .sessionManagement(SessionManagementConfigurer->{
                    SessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
                })
                .build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(){
        CorsConfiguration config = new CorsConfiguration();
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        config.setAllowedOrigins(List.of("http://localhost:5174", "http://127.0.0.1:5174", "http://127.0.0.1:5173", "http://localhost:5173"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", HttpMethod.PATCH.name(), "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
