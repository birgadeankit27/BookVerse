package com.bookverser.BookVerse.repository;



import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bookverser.BookVerse.entity.CartItem;

@Repository
public interface CartItemRepository  extends JpaRepository<CartItem, Long> {
	
	
	 Optional<CartItem> findByCartIdAndBookId(Long cartId, Long bookId);
	    List<CartItem> findAllByCartId(Long cartId);

}
