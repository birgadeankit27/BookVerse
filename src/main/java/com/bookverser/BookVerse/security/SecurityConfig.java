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
            .csrf(csrf -> csrf.ignoringRequestMatchers("/auth/**", "/api/books/**", "/api/carts/**", "/api/orders/**","/api/payments/**","/api/addresses/**"))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers("/auth/login", "/auth/register","/auth/forgot-password","/auth/reset-password").permitAll()
                .requestMatchers("/auth/register-admin").hasRole("ADMIN")

                // Book endpoints
                .requestMatchers(HttpMethod.POST, "/api/books/**").hasAnyRole("SELLER", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/books/**").hasAnyRole("CUSTOMER", "SELLER", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/books/**").hasAnyRole("SELLER", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/books/**").hasAnyRole("SELLER", "ADMIN")

                // Cart endpoints
                .requestMatchers("/api/carts/**").hasAnyAuthority("ROLE_CUSTOMER") // must match JWT authorities
                 
                // Payment endpoints
                .requestMatchers("/api/payments/**").hasAnyAuthority("ROLE_CUSTOMER")
                
                // Order endpoints
                .requestMatchers("/api/orders/**").hasAnyRole("CUSTOMER", "ADMIN")
                
                // Address endpoints
                .requestMatchers("/api/addresses/**").hasAnyRole("CUSTOMER", "ADMIN")
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
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
