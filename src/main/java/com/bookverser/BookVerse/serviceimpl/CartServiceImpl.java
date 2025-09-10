package com.bookverser.BookVerse.serviceimpl;
import org.springframework.stereotype.Service;

import com.bookverser.BookVerse.dto.AddToCartRequest;
import com.bookverser.BookVerse.dto.CartResponseDto;
import com.bookverser.BookVerse.dto.CheckoutRequest;
import com.bookverser.BookVerse.dto.UpdateCartRequest;
import com.bookverser.BookVerse.service.CartService;


@Service
public class CartServiceImpl {

	@Override
	public CartResponseDto addToCart(Long buyerId, AddToCartRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CartResponseDto updateCartItem(Long buyerId, Long bookId, UpdateCartRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CartResponseDto removeCartItem(Long buyerId, Long bookId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CartResponseDto getCartItems(Long buyerId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CartResponseDto clearCart(Long buyerId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object checkoutCart(Long buyerId, CheckoutRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

}
