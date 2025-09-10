package com.bookverser.BookVerse.config;

import com.bookverser.BookVerse.entity.Role;
import com.bookverser.BookVerse.entity.User;
import com.bookverser.BookVerse.repository.RoleRepository;
import com.bookverser.BookVerse.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class DataSeeder {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostConstruct
    public void init() {
        try {
            // ✅ Create roles if they don't exist
            String[] roleNames = {"ROLE_ADMIN", "ROLE_SELLER", "ROLE_CUSTOMER"};
            for (String roleName : roleNames) {
                roleRepository.findByName(roleName)
                        .orElseGet(() -> roleRepository.save(new Role(roleName)));
            }

            // ✅ Create default Admin user
            if (!userRepository.existsByEmail("admin@bookverse.com")) {
                Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                        .orElseThrow(() -> new RuntimeException("ROLE_ADMIN not found"));
                Role sellerRole = roleRepository.findByName("ROLE_SELLER")
                        .orElseThrow(() -> new RuntimeException("ROLE_SELLER not found"));

                Set<Role> adminRoles = new HashSet<>();
                adminRoles.add(adminRole);
                adminRoles.add(sellerRole);

                User admin = User.builder()
                        .name("admin")
                        .email("admin@bookverse.com")
                        .password(passwordEncoder.encode("admin123"))
                        .roles(adminRoles)
                        .city("Pune")                // default value
                        .state("Maharashtra")        // default value
                        .country("India")             // default value
                        .address("Admin Address")
                        .phone("+911234567890")
                        .build();
           


                userRepository.save(admin);
                System.out.println("✅ Created default admin: email='admin@bookverse.com', password='admin123'");
            }

            // ✅ Create default regular user
            if (!userRepository.existsByEmail("ankit@bookverse.com")) {
                Role sellerRole = roleRepository.findByName("ROLE_SELLER")
                        .orElseThrow(() -> new RuntimeException("ROLE_SELLER not found"));
                Role customerRole = roleRepository.findByName("ROLE_CUSTOMER")
                        .orElseThrow(() -> new RuntimeException("ROLE_CUSTOMER not found"));

                Set<Role> userRoles = new HashSet<>();
                userRoles.add(sellerRole);
                userRoles.add(customerRole);


                User user = User.builder()
                        .name("ankit")
                        .email("ankit@bookverse.com")
                        .password(passwordEncoder.encode("securePassword"))
                        .roles(userRoles)
                        .city("Mumbai")               // default value
                        .state("Maharashtra")         // default value
                        .country("India")             // default value
                        .address("User Address")
                        .phone("+919876543210")
                        .build();
          

                userRepository.save(user);
                System.out.println("✅ Created default user: email='ankit@bookverse.com', password='securePassword'");
            }
        } catch (Exception e) {
            // Prevent app from failing if seeder has an error
            e.printStackTrace();
        }
    }
}
