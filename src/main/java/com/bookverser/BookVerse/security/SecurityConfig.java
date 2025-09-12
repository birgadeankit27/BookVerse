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
            .csrf(csrf -> csrf.ignoringRequestMatchers("/auth/**", "/api/books/**", "/api/cart/**", "/api/orders/**")) // Disable CSRF for APIs
            .authorizeHttpRequests(auth -> auth
                // Auth endpoints
                .requestMatchers("/auth/login", "/auth/register","/auth/forgot-password","/auth/reset-password").permitAll() // Public access
                .requestMatchers("/auth/register-admin").hasRole("ADMIN") // Admin-only registration

                // Book endpoints
                .requestMatchers(HttpMethod.POST, "/api/books/**").hasAnyRole("SELLER", "ADMIN") // Add books
                .requestMatchers(HttpMethod.GET, "/api/books/**").hasAnyRole("CUSTOMER", "SELLER", "ADMIN") // Get books
                .requestMatchers(HttpMethod.PUT, "/api/books/**").hasAnyRole("SELLER", "ADMIN") // Update books
                .requestMatchers(HttpMethod.DELETE, "/api/books/**").hasAnyRole("SELLER", "ADMIN") // Delete books

                // Cart endpoints
                .requestMatchers("/api/carts/**").hasRole("CUSTOMER") // Only buyers can manage cart

                // Order endpoints
                .requestMatchers("/api/orders/**").hasAnyRole("CUSTOMER", "ADMIN") // Customers and admin can access orders

                .anyRequest().authenticated() // All other endpoints require authentication
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // Stateless JWT
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