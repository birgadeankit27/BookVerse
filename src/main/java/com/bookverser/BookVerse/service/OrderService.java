package com.bookverser.BookVerse.service;

import com.bookverser.BookVerse.dto.OrderResponseDto;
import com.bookverser.BookVerse.dto.PlaceOrderRequest;

import java.util.List;

public interface OrderService {

    OrderResponseDto placeOrder(PlaceOrderRequest request);

    OrderResponseDto getOrderById(Long orderId);

    // ✅ New method for "Get All My Orders"
    List<OrderResponseDto> getMyOrders(Long userId);

     
    void deleteOrder(Long orderId);}

