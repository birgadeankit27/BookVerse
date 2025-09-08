package com.bookverser.BookVerse.serviceimpl;

import com.bookverser.BookVerse.dto.LoginRequest;
import com.bookverser.BookVerse.dto.LoginResponse;
import com.bookverser.BookVerse.dto.SignupDto;
import com.bookverser.BookVerse.entity.Role;
import com.bookverser.BookVerse.entity.User;
import com.bookverser.BookVerse.repository.RoleRepository;
import com.bookverser.BookVerse.repository.UserRepository;
import com.bookverser.BookVerse.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public String register(SignupDto signupDto) {
        // Check if email already exists
        if (userRepository.existsByEmail(signupDto.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Validate and fetch role
        String requestedRole = "ROLE_" + signupDto.getRole().toUpperCase();
        if (!Set.of("ROLE_CUSTOMER", "ROLE_SELLER").contains(requestedRole)) {
            throw new RuntimeException("Invalid role. Only CUSTOMER or SELLER allowed.");
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

        return "User registered successfully";
    }

    @Override
    public String registerAdmin(SignupDto signupDto) {
        if (userRepository.existsByEmail(signupDto.getEmail())) {
            throw new RuntimeException("Email already exists");
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
        return "Admin registered successfully";
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public LoginResponse processLogin(LoginRequest loginRequest, String token) {
        // Get User entity
        User user = findByEmail(loginRequest.getEmail());

        // Prepare roles
        List<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        return new LoginResponse(
                token,
                user.getId(),
                user.getEmail(),
                user.getName(),
                roles
        );
    }
}