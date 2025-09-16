package com.bookverser.BookVerse.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bookverser.BookVerse.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByEmail(String email);

	Optional<User> findByName(String name);
	
    
	 List<User> findByRoles_Name(String role);   // role filtering

	    List<User> findByIsActive(boolean isActive); // status filtering

	    List<User> findByRoles_NameAndIsActive(String role, boolean isActive); // both filters

		boolean existsByEmail(String email);

		boolean existsByName(String name);
}
