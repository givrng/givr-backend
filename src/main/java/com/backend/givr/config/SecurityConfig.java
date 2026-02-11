package com.backend.givr.config;

import com.backend.givr.organization.oauth2.OrganizationOAuthFailureHandler;
import com.backend.givr.organization.oauth2.OrganizationOAuthService;
import com.backend.givr.organization.oauth2.OrganizationOauthSuccessHandler;
import com.backend.givr.organization.security.OrganizationDetailsService;
import com.backend.givr.shared.jwt.GivrCookie;
import com.backend.givr.shared.jwt.JwtAuthenticationFilter;
import com.backend.givr.shared.jwt.JwtUtil;
import com.backend.givr.shared.jwt.JwtValidationFilter;
import com.backend.givr.shared.service.TokenIdService;
import com.backend.givr.volunteer.oauth2.VolunteerOAuthFailureHandler;
import com.backend.givr.volunteer.oauth2.VolunteerOAuthService;
import com.backend.givr.volunteer.oauth2.VolunteerOAuthSuccessHandler;
import com.backend.givr.volunteer.security.VolunteerDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.authentication.AuthenticationProvider;
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

@Configuration
@EnableScheduling
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private VolunteerDetailsService volunteerDetailsService;
    @Autowired
    private OrganizationDetailsService organizationDetailsService;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private TokenIdService tokenIdService;
    @Autowired
    private GivrCookie givrCookie;

    @Value("${api.version}")
    private String apiVersion;
    @Value("${givr.allowed.origins}")
    private List<String> allowedOrigins;

    @Autowired
    private OrganizationOAuthService organizationOathService;
    @Autowired
    private VolunteerOAuthService volunteerOAuthService;

    @Value("${client.app.baseUrl}")
    private String baseUrl;

    @Bean
    PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationProvider volunteerDaoAuthProvider(){
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(volunteerDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    AuthenticationProvider organizationDaoProvider(){
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(organizationDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    @Order(1)
    SecurityFilterChain volunteerSecurityFilter(HttpSecurity httpSecurity) throws Exception {
        JwtAuthenticationFilter authFilter = new JwtAuthenticationFilter(givrCookie, volunteerDaoAuthProvider(), tokenIdService);
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
                .authenticationProvider(volunteerDaoAuthProvider())
                .addFilter(authFilter)
                .addFilterBefore(validationFilter, UsernamePasswordAuthenticationFilter.class)
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .build();
    }

    @Bean
    @Order(0)
    SecurityFilterChain volunteerOAuthSecurityFilter(HttpSecurity http){
        VolunteerOAuthSuccessHandler successHandler = new VolunteerOAuthSuccessHandler(volunteerDetailsService, baseUrl, givrCookie);
        VolunteerOAuthFailureHandler failureHandler = new VolunteerOAuthFailureHandler(baseUrl);
        return http.securityMatcher("/v1/api/volunteer/oauth2/**")
                .authorizeHttpRequests(req->req.anyRequest().authenticated())
                .oauth2Login(oauth->{
                    oauth.redirectionEndpoint(redirect->redirect.baseUri("/v1/api/volunteer/oauth2/code/*"))
                            .userInfoEndpoint(userInfo->{
                                userInfo.oidcUserService(volunteerOAuthService);
                            })
                            .authorizationEndpoint(auth->{
                                auth.baseUri("/v1/api/volunteer/oauth2/authorization");
                            })
                            .successHandler(successHandler)
                            .failureHandler(failureHandler);
                })
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.ALWAYS))
                .build();
    }

    @Bean
    @Order(3)
    SecurityFilterChain organizationSecurityFilter(HttpSecurity httpSecurity) throws Exception {
        JwtAuthenticationFilter authFilter = new JwtAuthenticationFilter(givrCookie, organizationDaoProvider(), tokenIdService);
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
                .authenticationProvider(organizationDaoProvider())
                .addFilter(authFilter)
                .sessionManagement(sm->sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(validationFilter, UsernamePasswordAuthenticationFilter.class)
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .build();
    }

    @Bean
    @Order(2)
    SecurityFilterChain organizationSecurityOAuthFilter(HttpSecurity httpSecurity){
        OrganizationOauthSuccessHandler successHandler = new OrganizationOauthSuccessHandler(organizationDetailsService, givrCookie, baseUrl);
        OrganizationOAuthFailureHandler failureHandler = new OrganizationOAuthFailureHandler(baseUrl);

        return httpSecurity.securityMatcher("/v1/api/organization/oauth2/**")
                .authorizeHttpRequests(auth->auth.anyRequest().authenticated())
                .oauth2Login(oauth->{
                    oauth.redirectionEndpoint(redirect->redirect.baseUri("/v1/api/organization/oauth2/code/*"))
                            .userInfoEndpoint(userInfo->{
                                userInfo.oidcUserService(organizationOathService);
                            })
                            .authorizationEndpoint(auth->{
                                auth.baseUri("/v1/api/organization/oauth2/authorization");
                            })
                            .successHandler(successHandler)
                            .failureHandler(failureHandler);
                })
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm->sm.sessionCreationPolicy(SessionCreationPolicy.ALWAYS))
                .build();
    }


    @Bean
    @Order(4)
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .securityMatcher("/v1/api/**")
                .csrf(AbstractHttpConfigurer::disable)
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
        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", HttpMethod.PATCH.name(), "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
