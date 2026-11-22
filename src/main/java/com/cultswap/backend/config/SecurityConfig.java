package com.cultswap.backend.config;

import com.cultswap.backend.security.JwtAuthenticationFilter;
import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()

                        // Public APIs
                        .requestMatchers(
                                "/api/auth/register",
                                "/api/auth/login",
                                "/error"
                        ).permitAll()

                        // Seller APIs
                        .requestMatchers(HttpMethod.POST, "/api/listings")
                        .hasRole("SELLER")

                        // Buyer APIs
                        .requestMatchers(HttpMethod.POST, "/api/listings/*/transfer-requests")
                        .hasRole("BUYER")

                        // Seller APIs
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/listings/*/transfer-requests"
                        ).hasRole("SELLER")

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/listings/*/transfer-requests/*/accept",
                                "/api/listings/*/transfer-requests/*/reject"
                        ).hasRole("SELLER")

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/listings/*/transfer-requests/*/complete"
                        ).hasRole("BUYER")

                        // All other APIs require authentication
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}