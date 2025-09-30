package com.bookverser.BookVerse.service;

import com.bookverser.BookVerse.dto.OrderResponseDto;
import com.bookverser.BookVerse.dto.PlaceOrderRequest;

public interface OrderService {
	public OrderResponseDto placeOrder(PlaceOrderRequest request);
	public OrderResponseDto getOrderById(Long orderId);
}
