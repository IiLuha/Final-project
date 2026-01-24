package com.itdev.finalproject.config;

import com.itdev.finalproject.security.CustomAccessDeniedHandler;
import com.itdev.finalproject.security.CustomAuthenticationEntryPoint;
import com.itdev.finalproject.security.JwtTokenFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;

import static com.itdev.finalproject.database.entity.Role.ADMIN;
import static com.itdev.finalproject.database.entity.Role.USER;

@Configuration
public class SecurityConfig {

    private final JwtTokenFilter filter;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;

    public SecurityConfig(JwtTokenFilter filter, CustomAuthenticationEntryPoint authenticationEntryPoint, CustomAccessDeniedHandler customAccessDeniedHandler) {
        this.filter = filter;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = customAccessDeniedHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http
                .formLogin(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
//                .cors(AbstractHttpConfigurer::disable)
                .sessionManagement(sessionManagement ->
                        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(urlConfig -> urlConfig
                        .requestMatchers("/v3/api-docs", "/swagger-ui/**")
                        .permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/users/auth", "/api/v1/users/register")
                        .permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/v1/locations", "/api/v1/locations/{id}",
                                "/api/v1/events/{id}", "/api/v1/events/my")
                        .hasAnyAuthority(USER.getAuthority(), ADMIN.getAuthority())
                        .requestMatchers(HttpMethod.GET, "/api/v1/users/{id}", "/api/v1/users")
                        .hasAuthority(ADMIN.getAuthority())

                        .requestMatchers(HttpMethod.POST, "/api/v1/events/search",
                                "/api/v1/events/registrations/{id}", "/api/v1/events")
                        .hasAnyAuthority(USER.getAuthority(), ADMIN.getAuthority())
                        .requestMatchers(HttpMethod.POST, "/api/v1/locations/create")
                        .hasAuthority(ADMIN.getAuthority())

                        .requestMatchers(HttpMethod.DELETE, "/api/v1/events/{id}",
                                "/api/v1/events/registrations/cancel/{id}")
                        .hasAnyAuthority(USER.getAuthority(), ADMIN.getAuthority())
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/locations/{id}", "/api/v1/users/{id}")
                        .hasAuthority(ADMIN.getAuthority())

                        .requestMatchers(HttpMethod.PUT, "/api/v1/events/{id}", "/api/v1/users/{id}")
                        .hasAnyAuthority(USER.getAuthority(), ADMIN.getAuthority())
                        .requestMatchers(HttpMethod.PUT, "/api/v1/locations/{id}")
                        .hasAuthority(ADMIN.getAuthority())

                        .anyRequest().authenticated())
                .addFilterBefore(filter, AnonymousAuthenticationFilter.class)
                .exceptionHandling(exception ->
                        exception.authenticationEntryPoint(authenticationEntryPoint)
                                .accessDeniedHandler(accessDeniedHandler));
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration
    ) {
        return configuration.getAuthenticationManager();
    }
}
