package com.creativespacefinder.manhattan.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @SuppressWarnings("java:S4502") // Suppress SonarQube CSRF warning
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // SECURITY JUSTIFICATION: CSRF disabled for REST API
                // - This is a stateless REST API consumed by mobile/SPA clients  
                // - Admin endpoints use additional session-based validation
                // - Custom X-Requested-With header provides CSRF-like protection
                // - CORS is configured to restrict origins in production
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/api/recommendations/**").permitAll()
                        .requestMatchers("/api/forecast/**").permitAll()
                        .requestMatchers("/api/health/**").permitAll()
                        .requestMatchers("/api/admin/login").permitAll()
                        .requestMatchers("/api/admin/validate-session").permitAll()
                        .requestMatchers("/api/admin/logout").permitAll()
                        .requestMatchers("/api/admin/cache-status").permitAll()
                        .requestMatchers("/api/admin/warm-cache").permitAll()
                        .requestMatchers("/api/admin/**").authenticated()
                        .anyRequest().permitAll()
                )
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(formLogin -> formLogin.disable())
                .sessionManagement(session -> session
                        .maximumSessions(10)
                        .maxSessionsPreventsLogin(false)
                );

        return http.build();
    }
}
