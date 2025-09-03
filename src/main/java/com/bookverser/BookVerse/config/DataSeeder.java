package com.bookverser.BookVerse.config;

import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.bookverser.BookVerse.entity.Role;
import com.bookverser.BookVerse.entity.User;
import com.bookverser.BookVerse.repository.RoleRepository;
import com.bookverser.BookVerse.repository.UserRepository;

@Configuration
public class DataSeeder {
	 @Bean
	    CommandLineRunner initData(RoleRepository roleRepo, UserRepository userRepo) {
	        return args -> {
	        	Role adminRole = roleRepo.findByName("ADMIN")
	        	        .orElseGet(() -> roleRepo.save(new Role(null, "ADMIN")));
	            Role sellerRole = roleRepo.findByName("SELLER").orElseGet(() -> roleRepo.save(new Role(null, "SELLER")));
	            Role customerRole = roleRepo.findByName("CUSTOMER").orElseGet(() -> roleRepo.save(new Role(null, "CUSTOMER")));

	            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

	            if (userRepo.count() == 0) {
	                userRepo.save(new User(null, "Admin User", "admin@example.com", encoder.encode("admin123"),
	                        Set.of(adminRole), "Admin Address", "1234567890", null));
	                userRepo.save(new User(null, "Seller User", "seller@example.com", encoder.encode("seller123"),
	                        Set.of(sellerRole), "Seller Address", "9876543210", null));
	                userRepo.save(new User(null, "Customer User", "customer@example.com", encoder.encode("customer123"),
	                        Set.of(customerRole), "Customer Address", "5555555555", null));
	            }
	        };
	    }
}
