package com.bookverser.BookVerse.service;

import com.bookverser.BookVerse.dto.OrderResponseDto;
import com.bookverser.BookVerse.dto.PlaceOrderRequest;

public interface OrderService {
    OrderResponseDto placeOrder(PlaceOrderRequest request);
}
