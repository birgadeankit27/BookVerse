package com.bookverser.BookVerse.controller;

import com.bookverser.BookVerse.dto.ChangePasswordRequest;
import com.bookverser.BookVerse.dto.ForgotPasswordRequest;
import com.bookverser.BookVerse.dto.LoginRequest;
import com.bookverser.BookVerse.dto.LoginResponse;
import com.bookverser.BookVerse.dto.ResetPasswordRequest;
import com.bookverser.BookVerse.dto.SignupDto;
import com.bookverser.BookVerse.dto.UpdateProfileRequest;
import com.bookverser.BookVerse.dto.UserDto;
import com.bookverser.BookVerse.dto.UserResponseDto;
import com.bookverser.BookVerse.dto.UserStatusResponse;
import com.bookverser.BookVerse.entity.User;
import com.bookverser.BookVerse.security.JwtUtil;
import com.bookverser.BookVerse.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;


import java.io.IOException;

import java.util.List;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/auth")
public class UserController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public UserController(UserService userService,
                          AuthenticationManager authenticationManager,
                          JwtUtil jwtUtil) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    // ==================== REGISTER ====================
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody SignupDto signupDto) {
        try {
            String message = userService.register(signupDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(message);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Email already exists")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
            } else if (e.getMessage().contains("Invalid role")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Registration failed: " + e.getMessage());
            }
        }
    }

    // ==================== REGISTER ADMIN ====================
    @PostMapping("/register-admin")
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> registerAdmin(@Valid @RequestBody SignupDto signupDto) {
        try {
            String message = userService.registerAdmin(signupDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(message);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Email already exists")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Admin registration failed: " + e.getMessage());
            }
        }
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

            // Generate access and refresh tokens
            String accessToken = jwtUtil.generateAccessToken(userDetails);
            String refreshToken = jwtUtil.generateRefreshToken(userDetails);

            // Prepare response using service
            LoginResponse response = userService.processLogin(loginRequest, accessToken, refreshToken);

            return ResponseEntity.ok(response);

        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid email or password");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Login failed: " + e.getMessage());
        }
    }
    // ==================== GET USER BY EMAIL ====================
    @GetMapping("/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUserByEmail(@PathVariable String email) {
        try {
            User user = userService.findByEmail(email);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
        
    }     

        // ==================== ✅ GET CURRENT LOGGED-IN USER ====================
    
        @GetMapping("/get-current-user")
        public ResponseEntity<UserDto> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
            if (userDetails == null) {
                return ResponseEntity.status(401).build();
            }
            UserDto userDto = userService.getUserByEmail(userDetails.getUsername()); // username = email
            return ResponseEntity.ok(userDto);
        }
        
        
        // ==================== ✅ UPDATE PROFILE ====================
        
        @PutMapping("/update-profile")
        public ResponseEntity<UserDto> updateProfile(
                @AuthenticationPrincipal UserDetails userDetails,
                @Valid @RequestBody UpdateProfileRequest request) {

            if (userDetails == null) {
                return ResponseEntity.status(401).build(); // Unauthorized if no JWT token
            }

            UserDto updatedUser = userService.updateUserProfile(userDetails.getUsername(), request);
            return ResponseEntity.ok(updatedUser);
        }
        
        // ==================== ✅ Change Password ====================
        
        @PutMapping("/change-password")
        public ResponseEntity<?> changePassword(
                @AuthenticationPrincipal UserDetails userDetails,
                @Valid @RequestBody ChangePasswordRequest request) {

            if (userDetails == null) {
                return ResponseEntity.status(401).body("Unauthorized: Please login first");
            }

            try {
                String message = userService.changePassword(userDetails.getUsername(), request);
                return ResponseEntity.ok(message);
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().body(e.getMessage());
            }
        }
        
        // ==================== ✅ Forgot Password (Request OTP)  ====================
        
        @PostMapping("/forgot-password")
            public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
            try {
                String response = userService.forgotPassword(request); // Generates OTP and sends email
                return ResponseEntity.ok().body(response);
            } catch (RuntimeException e) {
                // 404 Not Found if email or phone is invalid
                if (e.getMessage().contains("not found")) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(e.getMessage());
                }
                // Other server errors
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Failed to send OTP: " + e.getMessage());
            }
        }
        
     // ==================== ✅ Reset Password (Request OTP)  ====================
            @PostMapping("/reset-password")
            public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
                try {
                    String response = userService.resetPassword(request);
                    return ResponseEntity.ok(response);
                } catch (RuntimeException e) {
                    return ResponseEntity.status(400).body(e.getMessage());
                }
        }

         // ==================== ✅ Upload Profile Picture API  ====================
            @PostMapping("/upload-profile-picture")
            public ResponseEntity<?> uploadProfilePicture(
                    @RequestParam("file") MultipartFile file,
                    @AuthenticationPrincipal UserDetails userDetails,
                    HttpServletRequest request) {
                try {
                    // ✅ Pass email (username in Spring Security) to service
                    String fileUrl = userService.uploadProfilePicture(file, userDetails.getUsername());

                    // Build full URL
                    String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
                    String imageUrl = baseUrl + fileUrl;

                    return ResponseEntity.ok(new UploadResponse("Profile picture uploaded", imageUrl));

                } catch (IOException e) {
                    return ResponseEntity.status(500).body("Error uploading file: " + e.getMessage());
                }
            }

            // ✅ Record must be declared at class level
            public record UploadResponse(String message, String imageUrl) {}


       // ==================== ✅  List All Users API (Admin Only)  ====================
            
            @GetMapping("/admin/users")
            @PreAuthorize("hasRole('ADMIN')")  // Only ADMIN can access
            public ResponseEntity<List<UserResponseDto>> listUsers(
                    @RequestParam(required = false) String role,
                    @RequestParam(required = false) String status) {

                List<UserResponseDto> users = userService.listUsers(role, status);
                return ResponseEntity.ok(users);
            }
            
            // ====================✅ Block/Unblock API ====================
            @PutMapping("/admin/users/{id}/status")
            @PreAuthorize("hasRole('ADMIN')")
            public ResponseEntity<UserStatusResponse> updateUserStatus(
                    @PathVariable Long id,
                    @RequestParam boolean active) {
                UserStatusResponse response = userService.updateUserStatus(id, active);
                return ResponseEntity.ok(response);
            }
            
         // ==================== ✅ ADMIN: DELETE ORDER ====================

            @DeleteMapping("/admin/orders/{orderId}")
            @PreAuthorize("hasRole('ADMIN')")  // ✅ Only Admin can access
            public ResponseEntity<String> deleteOrder(@PathVariable Long orderId) {
                try {
                    userService.deleteOrderByAdmin(orderId);
                    return ResponseEntity.ok("✅ Order deleted successfully!");
                } catch (RuntimeException e) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("❌ " + e.getMessage());
                }
            }

            
            

}