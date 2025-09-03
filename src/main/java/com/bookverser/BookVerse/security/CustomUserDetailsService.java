package com.bookverser.BookVerse.security;

import java.util.Collection;
import java.util.*;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority; 
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import org.springframework.stereotype.Service;

import com.bookverser.BookVerse.entity.User;
import com.bookverser.BookVerse.exception.UsernameNotFoundException;
import com.bookverser.BookVerse.repository.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

	 @Autowired
	    private UserRepository userRepository;


	@Override
	public UserDetails loadUserByUsername(String email)  {
		// TODO Auto-generated method stub
		  User user = userRepository.findByEmail(email)
	                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

		  Collection<GrantedAuthority> authorities = Optional.ofNullable(user.getRoles())
	                .orElse(Collections.emptySet())
	                .stream()
	                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName())) // e.g. ROLE_ADMIN
	                .collect(Collectors.toList());

	        // Return UserDetails (username = email)
	        return new org.springframework.security.core.userdetails.User(
	                user.getEmail(),
	                user.getPassword(),
	                authorities
	        );
	}

}
