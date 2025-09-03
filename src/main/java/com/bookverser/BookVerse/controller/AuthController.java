package com.bookverser.BookVerse.controller;

import com.bookverser.BookVerse.dto.LoginRequest;
import com.bookverser.BookVerse.dto.LoginResponse;
import com.bookverser.BookVerse.entity.User;
import com.bookverser.BookVerse.repository.UserRepository;
import com.bookverser.BookVerse.security.CustomUserDetailsService;
import com.bookverser.BookVerse.security.JwtUtil;


import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/authController")
public class AuthController {

    @Autowired
    private AuthenticationManager authManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepo;
    @Autowired
    private CustomUserDetailsService userDetailsService;

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest loginreq) {
        // 1️⃣ Authenticate email/password
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginreq.getEmail(), loginreq.getPassword())
        );

        // 2️⃣ Fetch user from DB
        User user = userRepo.findByEmail(loginreq.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 3️⃣ Extract roles (from User entity)
        Set<String> roles = user.getRoles().stream()
                .map(role -> role.getName())
                .collect(Collectors.toSet());

        // 4️⃣ Generate JWT with roles
        String token = jwtUtil.generateToken(user.getEmail());

        // 5️⃣ Return structured response
        return new LoginResponse(token, 
                                 new java.util.Date(System.currentTimeMillis() + (1000 * 60 * 60)), // 1 hr expiry
                                 user.getEmail(), 
                                 roles.stream().toList());
    }
    
    @PostMapping("/register")
    public String register(@RequestBody User user) {
        // Encode password
        user.setPassword(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode(user.getPassword()));
        userRepo.save(user);
        return "User registered successfully";
    }
}
