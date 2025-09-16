package com.bookverser.BookVerse.serviceimpl;

import com.bookverser.BookVerse.dto.ChangePasswordRequest;
import com.bookverser.BookVerse.dto.ForgotPasswordRequest;
import com.bookverser.BookVerse.dto.LoginRequest;
import com.bookverser.BookVerse.dto.LoginResponse;
import com.bookverser.BookVerse.dto.ResetPasswordRequest;
import com.bookverser.BookVerse.dto.SignupDto;
import com.bookverser.BookVerse.dto.UpdateProfileRequest;
import com.bookverser.BookVerse.dto.UserDto;
import com.bookverser.BookVerse.dto.UserResponseDto;
import com.bookverser.BookVerse.entity.PasswordResetToken;
import com.bookverser.BookVerse.entity.Role;
import com.bookverser.BookVerse.entity.User;
import com.bookverser.BookVerse.repository.PasswordResetTokenRepository;
import com.bookverser.BookVerse.repository.RoleRepository;
import com.bookverser.BookVerse.repository.UserRepository;
import com.bookverser.BookVerse.service.UserService;

import java.io.IOException;

import jakarta.transaction.Transactional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final PasswordResetTokenRepository tokenRepository;
    private final JavaMailSender mailSender;


    public UserServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder,
                           ModelMapper modelMapper,
                           PasswordResetTokenRepository tokenRepository,
                           JavaMailSender mailSender) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.modelMapper = modelMapper;
        this.tokenRepository = tokenRepository;
        this.mailSender = mailSender;
    }
     
    @Transactional
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
                .city(signupDto.getCity() != null ? signupDto.getCity() : "Unknown")
                .state(signupDto.getState() != null ? signupDto.getState() : "Unknown")
                .country(signupDto.getCountry() != null ? signupDto.getCountry() : "Unknown")
                .roles(new HashSet<>(Set.of(role)))
                .isActive(true)
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
	public UserDto getUserByEmail(String email) {
		User user = userRepository.findByEmail(email)
	            .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
	    return mapToDto(user);
	}

	// ✅ Helper method to map Entity → DTO
	private UserDto mapToDto(User user) {
	    // Convert role set → single role string
	    String roles = user.getRoles().stream()
	            .map(Role::getName)
	            .reduce((r1, r2) -> r1 + ", " + r2)
	            .orElse("CUSTOMER");

	    UserDto dto = new UserDto();
	    dto.setId(user.getId());
	    dto.setName(user.getName());
	    dto.setEmail(user.getEmail());
	    dto.setRole(roles);
	    dto.setAddress(user.getAddress());
	    dto.setPhone(user.getPhone());
	    dto.setCity(user.getCity());
	    dto.setState(user.getState());
	    dto.setCountry(user.getCountry());

	    return dto;
    }
	
	

	@Override
	public LoginResponse processLogin(LoginRequest loginRequest, String accessToken, String refreshToken) {
		 User user = userRepository.findByEmail(loginRequest.getEmail())
	                .orElseThrow(() -> new RuntimeException("User not found with email: " + loginRequest.getEmail()));

	        // Prepare roles
	        List<String> roles = user.getRoles().stream()
	                .map(Role::getName)
	                .collect(Collectors.toList());

	        return new LoginResponse(
	                accessToken,
	                refreshToken,
	                user.getId(),
	                user.getEmail(),
	                user.getName(),
	                roles
	        );
	}

	
	
	@Override
	public UserDto updateUserProfile(String email, UpdateProfileRequest request) {
		 User user = userRepository.findByEmail(email)
	                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

	        // ✅ Update allowed fields
	        user.setName(request.getName());
	        user.setPhone(request.getPhone());
	        user.setAddress(request.getAddress());
	        user.setCity(request.getCity());
	        user.setState(request.getState());
	        user.setCountry(request.getCountry());
	        User updatedUser = userRepository.save(user);

	        // ✅ Use ModelMapper to map User → UserDto
	        UserDto dto = modelMapper.map(updatedUser, UserDto.class);

	        // ✅ Manually handle roles because ModelMapper may not handle Set<Role> → String directly
	        dto.setRole(updatedUser.getRoles().stream()
	                .findFirst()
	                .map(role -> role.getName())
	                .orElse("CUSTOMER"));

	        return dto;
	}
	

	@Override
	public String changePassword(String email, ChangePasswordRequest request) {
		 User user = userRepository.findByEmail(email)
	                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

	        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
	            throw new RuntimeException("Invalid old password");
	        }

	        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
	        userRepository.save(user);

	        return "Password changed successfully";
	}
	

	@Override
	public String forgotPassword(ForgotPasswordRequest request) {

		// new code
		
		 String email = request.getEmail();

		    User user = userRepository.findByEmail(email)
		            .orElseThrow(() -> new RuntimeException("User not found"));

		    // Generate OTP
//		    String otp = String.valueOf(100000 + new Random().nextInt(900000));
//		    LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(5);


	        // ✅ Generate secure OTP (6-digit)
	        SecureRandom rnd = new SecureRandom();
	        String otp = String.format("%06d", rnd.nextInt(1_000_000));

	        // ✅ Hash the OTP before saving
	        String hashedOtp = passwordEncoder.encode(otp);

	        // ✅ Set expiry time
	        Instant expiry = Instant.now().plus(5, ChronoUnit.MINUTES);
	    
	        // ✅ Create or update token record
	        PasswordResetToken token = tokenRepository.findByEmail(email)
	                .orElse(new PasswordResetToken());

	        token.setEmail(email);
	        token.setOtpHash(hashedOtp);
	        token.setExpiryTime(expiry);
	        token.setUsed(false);

	        tokenRepository.save(token);
		    // Send OTP via email
		    SimpleMailMessage message = new SimpleMailMessage();
		    message.setTo(email);
		    message.setSubject("BookVerse - Password Reset OTP");
		    message.setText("Your OTP is: " + otp + "\nThis OTP is valid for 5 minutes.");
		    try {
	            mailSender.send(message);
	        } catch (MailException e) {
	            throw new RuntimeException("Failed to send OTP. Please try again later.", e);
	        }

		    return "OTP sent successfully to " + email;
	}

	@Override
	public String resetPassword(ResetPasswordRequest request) {
		   PasswordResetToken token = tokenRepository.findByEmail(request.getEmail())
		            .orElseThrow(() -> new RuntimeException("Invalid email or OTP"));

		 // ✅ Check if OTP already used
		    if (token.isUsed()) {
		        throw new RuntimeException("OTP has already been used");
		    }

		    // ✅ Check if OTP is expired (using Instant now)
		    if (token.getExpiryTime().isBefore(Instant.now())) {
		        throw new RuntimeException("OTP has expired");
		    }

		    // ✅ Verify OTP with hash
		    if (!passwordEncoder.matches(request.getOtp(), token.getOtpHash())) {
		        throw new RuntimeException("Invalid OTP");
		    }

		    // Fetch user
		    User user = userRepository.findByEmail(request.getEmail())
		            .orElseThrow(() -> new RuntimeException("User not found"));

		    // ✅ Check if new password is same as old password
		    if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
		        throw new RuntimeException("New password cannot be the same as the old password");
		    }

		    // Encode and set new password
		    user.setPassword(passwordEncoder.encode(request.getNewPassword()));
		    userRepository.save(user);

		    return "Password has been reset successfully";
	}
	
	
	@Value("${file.upload-dir}")
	private String uploadDir;

	// ✅ Max size = 2 MB (you can change)
	private static final long MAX_FILE_SIZE = 2 * 1024 * 1024;

	// ✅ Allowed content types
	private static final List<String> ALLOWED_TYPES = List.of("image/jpeg", "image/png");

	@Override
	public String uploadProfilePicture(MultipartFile file, String email) throws IOException {
	    // ================== ✅ VALIDATIONS ==================

	    if (file.isEmpty()) {
	        throw new RuntimeException("File is empty");
	    }

	    if (file.getSize() > MAX_FILE_SIZE) {
	        throw new RuntimeException("File size exceeds 2MB limit");
	    }

	    if (!ALLOWED_TYPES.contains(file.getContentType())) {
	        throw new RuntimeException("Only JPG and PNG files are allowed");
	    }

	    // ================== ✅ SAVE FILE ==================
	    Path uploadPath = Paths.get(uploadDir);
	    if (!Files.exists(uploadPath)) {
	        Files.createDirectories(uploadPath);
	    }

	    String fileName = email + "_profile_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
	    Path filePath = uploadPath.resolve(fileName);

	    Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

	    String fileUrl = "/uploads/" + fileName;

	    // ✅ Update user profile picture URL in DB
	    User user = userRepository.findByEmail(email)
	            .orElseThrow(() -> new RuntimeException("User not found"));
	    user.setProfilePictureUrl(fileUrl);
	    userRepository.save(user);

	    return fileUrl;
	}

	@Override
	public List<UserResponseDto> listUsers(String role, String status) {
		List<User> users;

        if (role != null && status != null) {
            boolean isActive = status.equalsIgnoreCase("ACTIVE");
            users = userRepository.findByRoles_NameAndIsActive(role.toUpperCase(), isActive);
        } else if (role != null) {
            users = userRepository.findByRoles_Name(role.toUpperCase());
        } else if (status != null) {
            boolean isActive = status.equalsIgnoreCase("ACTIVE");
            users = userRepository.findByIsActive(isActive);
        } else {
            users = userRepository.findAll();
        }

        return users.stream()
                .map(user -> {
                    UserResponseDto dto = modelMapper.map(user, UserResponseDto.class);
                    // Convert roles set to a single role string
                    dto.setRole(user.getRoles().stream()
                            .findFirst()
                            .map(r -> r.getName())
                            .orElse("USER"));
                    dto.setStatus(user.isActive() ? "ACTIVE" : "INACTIVE");
                    return dto;
                })
                .collect(Collectors.toList());
	}


	
}