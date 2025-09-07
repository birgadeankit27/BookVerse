package com.bookverser.BookVerse.controller;

import com.bookverser.BookVerse.dto.LoginRequest;
import com.bookverser.BookVerse.dto.LoginResponse;
import com.bookverser.BookVerse.dto.SignupDto;
import com.bookverser.BookVerse.entity.Role;
import com.bookverser.BookVerse.entity.User;
import com.bookverser.BookVerse.repository.RoleRepository;
import com.bookverser.BookVerse.repository.UserRepository;
import com.bookverser.BookVerse.security.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.HashSet;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthController(PasswordEncoder passwordEncoder,
                          UserRepository userRepository,
                          RoleRepository roleRepository,
                          AuthenticationManager authenticationManager,
                          JwtUtil jwtUtil) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    // ==================== REGISTER ====================
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody SignupDto signupDto) {
        // Check if email already exists
        if (userRepository.existsByEmail(signupDto.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Email already exists");
        }

        // Validate and fetch role
        String requestedRole = "ROLE_" + signupDto.getRole().toUpperCase();
        if (!Set.of("ROLE_CUSTOMER", "ROLE_SELLER").contains(requestedRole)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid role. Only CUSTOMER or SELLER allowed.");
        }

        Role role = roleRepository.findByName(requestedRole)
                .orElseThrow(() -> new RuntimeException(requestedRole + " role not found"));

        // Build and save the user
        User user = User.builder()
                .name(signupDto.getName())
                .email(signupDto.getEmail())
                .password(passwordEncoder.encode(signupDto.getPassword()))
                .address(signupDto.getAddress())
                .phone(signupDto.getPhone())
                .roles(new HashSet<>(Set.of(role)))
                .build();

        userRepository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body("User registered successfully");
    }

    // ==================== REGISTER ADMIN ====================
    @PostMapping("/register-admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> registerAdmin(@Valid @RequestBody SignupDto signupDto) {
        if (userRepository.existsByEmail(signupDto.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Email already exists");
        }

        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseThrow(() -> new RuntimeException("ROLE_ADMIN role not found"));

        User user = User.builder()
                .name(signupDto.getName())
                .email(signupDto.getEmail())
                .password(passwordEncoder.encode(signupDto.getPassword()))
                .address(signupDto.getAddress())
                .phone(signupDto.getPhone())
                .roles(new HashSet<>(Set.of(adminRole)))
                .build();

        userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Admin registered successfully");
    }

    // ==================== LOGIN ====================
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            // Authenticate
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            // Get UserDetails
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            // Get User entity
            User user = userRepository.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Generate token
            String token = jwtUtil.generateToken(userDetails);

            // Prepare response
            List<String> roles = user.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.toList());

            LoginResponse response = new LoginResponse(
                    token,
                    user.getId(),
                    user.getEmail(),
                    user.getName(),
                    roles
            );

            return ResponseEntity.ok(response);

        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid email or password");
        }
    }

}
