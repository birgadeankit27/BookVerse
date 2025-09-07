package com.bookverser.BookVerse.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final JwtFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtUtil jwtUtil, JwtFilter jwtAuthenticationFilter) {
        this.jwtUtil = jwtUtil;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.ignoringRequestMatchers("/auth/**", "/api/books/**")) // Disable CSRF for /api/books
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/login", "/auth/register").permitAll() // Public access
                .requestMatchers("/auth/register-admin").hasRole("ADMIN") // Restrict to ROLE_ADMIN
                .requestMatchers(HttpMethod.POST, "/api/books/**").hasAnyRole("SELLER", "ADMIN") // POST for adding books
                .requestMatchers(HttpMethod.GET, "/api/books/**").hasAnyRole("CUSTOMER", "SELLER", "ADMIN") // GET for retrieving books
                .anyRequest().authenticated() // All other endpoints require authentication
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // Stateless for JWT
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}