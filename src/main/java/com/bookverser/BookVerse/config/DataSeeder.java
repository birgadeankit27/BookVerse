package com.bookverser.BookVerse.config;

import com.bookverser.BookVerse.entity.Address;
import com.bookverser.BookVerse.entity.Role;
import com.bookverser.BookVerse.entity.User;
import com.bookverser.BookVerse.repository.AddressRepository;
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
    private AddressRepository addressRepository;

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
            if (!userRepository.existsByEmail("bookverse.work@gmail.com")) {
                Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                        .orElseThrow(() -> new RuntimeException("ROLE_ADMIN not found"));
                Role sellerRole = roleRepository.findByName("ROLE_SELLER")
                        .orElseThrow(() -> new RuntimeException("ROLE_SELLER not found"));

                Set<Role> adminRoles = new HashSet<>();
                adminRoles.add(adminRole);
                adminRoles.add(sellerRole);

                User admin = User.builder()
                        .name("admin")
                        .email("bookverse.work@gmail.com")
                        .password(passwordEncoder.encode("admin@123***"))
                        .roles(adminRoles)
                        
                        .phone("+911234567890")
                        .build();

                userRepository.save(admin);

                // ✅ Create default address for Admin
                Address adminAddress = Address.builder()
                        .city("Pune")
                        .state("Maharashtra")
                        .country("India")
                        .user(admin)
                        .build();

                addressRepository.save(adminAddress);

                System.out.println("✅ Created default admin with address: email='bookverse.work@gmail.com', password='admin@123***'");
            }

            // ✅ Create default regular user
            if (!userRepository.existsByEmail("ankitbirgade@gmail.com")) {
                Role sellerRole = roleRepository.findByName("ROLE_SELLER")
                        .orElseThrow(() -> new RuntimeException("ROLE_SELLER not found"));
                Role customerRole = roleRepository.findByName("ROLE_CUSTOMER")
                        .orElseThrow(() -> new RuntimeException("ROLE_CUSTOMER not found"));

                Set<Role> userRoles = new HashSet<>();
                userRoles.add(sellerRole);
                userRoles.add(customerRole);

                User user = User.builder()
                        .name("ankit")
                        .email("ankitbirgade@gmail.com")
                        .password(passwordEncoder.encode("securePassword"))
                        .roles(userRoles)
                       
                        .phone("+919876543210")
                        .build();

                userRepository.save(user);

                // ✅ Create default address for User
                Address userAddress = Address.builder()
                        .city("Mumbai")
                        .state("Maharashtra")
                        .country("India")
                        .user(user)
                        .build();

                addressRepository.save(userAddress);

                System.out.println("✅ Created default user with address: email='ankitbirgade@gmail.com', password='securePassword'");
            }
        } catch (Exception e) {
            // Prevent app from failing if seeder has an error
            e.printStackTrace();
        }
    }
}
