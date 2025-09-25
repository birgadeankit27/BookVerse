package com.bookverser.BookVerse.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bookverser.BookVerse.entity.Book;
import com.bookverser.BookVerse.entity.Cart;
import com.bookverser.BookVerse.entity.CartItem;
import com.bookverser.BookVerse.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

//	Optional<User> findByCartIdAndBookId(Long id, Long id2);

//	Optional<User> findByBuyerId(Long buyerId);

//	Optional<User> findByCustomerId(Long customerId);
	 

	

	 Optional<Cart> findByCustomerId(Long customerId);

//	Optional<CartItem> findByCartIdAndBookId(Long id, Long id2);

	 
//	List<CartItem> findAllByCartId(Long id);

//	CartItem save(CartItem cartItem);
	

}
