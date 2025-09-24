package com.bookverser.BookVerse.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bookverser.BookVerse.entity.Cart;
import com.bookverser.BookVerse.entity.User;

@Repository
public interface CartRepository  extends JpaRepository<Cart, Long> {
	   Optional<Cart> findByCustomer(User customer);
}
