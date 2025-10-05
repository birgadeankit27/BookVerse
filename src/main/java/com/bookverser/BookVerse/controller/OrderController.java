package com.bookverser.BookVerse.controller;

import com.bookverser.BookVerse.dto.OrderResponseDto;
import com.bookverser.BookVerse.security.CustomUserDetails;
import com.bookverser.BookVerse.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    // 🔹 Get order by ID
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponseDto> getOrderById(@PathVariable Long orderId) {
        OrderResponseDto response = orderService.getOrderById(orderId);
        return ResponseEntity.ok(response);
    }

    // 🔹 Get all orders for logged-in user
    @GetMapping("/my")
    public ResponseEntity<List<OrderResponseDto>> getMyOrders(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        List<OrderResponseDto> orders = orderService.getMyOrders(userDetails.getId());
        return ResponseEntity.ok(orders);
    }
}
