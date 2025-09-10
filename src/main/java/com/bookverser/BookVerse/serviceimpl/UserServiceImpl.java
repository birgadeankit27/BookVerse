package com.bookverser.BookVerse.serviceimpl;

import com.bookverser.BookVerse.dto.LoginRequest;
import com.bookverser.BookVerse.dto.LoginResponse;
import com.bookverser.BookVerse.dto.SignupDto;
import com.bookverser.BookVerse.dto.UpdateProfileRequest;
import com.bookverser.BookVerse.dto.UserDto;
import com.bookverser.BookVerse.entity.Role;
import com.bookverser.BookVerse.entity.User;
import com.bookverser.BookVerse.repository.RoleRepository;
import com.bookverser.BookVerse.repository.UserRepository;
import com.bookverser.BookVerse.service.UserService;

import jakarta.transaction.Transactional;

import org.modelmapper.ModelMapper;
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
    private final ModelMapper modelMapper;


    public UserServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder,
                           ModelMapper modelMapper) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.modelMapper = modelMapper;
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
	      
	        // ✅ Use ModelMapper instead of mapToDto
	       // return modelMapper.map(user, UserDto.class);
	    }
//	  ✅ Helper method to map Entity → DTO
	private UserDto mapToDto(User user) {
        String roles = user.getRoles().stream()
                .map(Role::getName)
                .reduce((r1, r2) -> r1 + ", " + r2) // Join roles into single string
                .orElse("USER");

        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setRole(roles);
        dto.setAddress(user.getAddress());
        dto.setPhone(user.getPhone());
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
//	        user.setCity(request.getCity());
//	        user.setState(request.getState());
//	        user.setCountry(request.getCountry());
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


	
}