package com.bookverser.BookVerse.controller;


import com.bookverser.BookVerse.dto.BulkOrderStatusUpdateRequest;



import com.bookverser.BookVerse.dto.OrderResponseDto;
import com.bookverser.BookVerse.dto.OrderSummaryDto;
import com.bookverser.BookVerse.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

import com.bookverser.BookVerse.dto.AdminOrderResponseDto;
import com.bookverser.BookVerse.dto.OrderDTO;
import com.bookverser.BookVerse.dto.OrderResponseDto;
import com.bookverser.BookVerse.dto.PlaceOrderRequest;
import com.bookverser.BookVerse.security.CustomUserDetails;
import com.bookverser.BookVerse.service.OrderService;
import jakarta.validation.Valid;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {


    private final OrderService orderService;

    // =============== CUSTOMER: GET MY ORDERS ===============
    @GetMapping("/my")
    public List<OrderResponseDto> getMyOrders(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            throw new RuntimeException("Unauthorized: Please login");
        }
        return orderService.getMyOrders(userDetails.getUsername());
    }

    // =============== ADMIN: GET ALL ORDERS ===============
    @GetMapping("/admin/orders")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<OrderSummaryDto> getAllOrders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(required = false) Long customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);

        LocalDate from = fromDate != null ? LocalDate.parse(fromDate) : null;
        LocalDate to = toDate != null ? LocalDate.parse(toDate) : null;

        return orderService.getAllOrders(status, from, to, customerId, pageable);
    }

    
 // =============== ADMIN: BULK UPDATE ORDER STATUS ===============
    @PatchMapping("/admin/orders/status/bulk")
    @PreAuthorize("hasRole('ADMIN')")
    public List<OrderResponseDto> bulkUpdateOrderStatus(@RequestBody BulkOrderStatusUpdateRequest request) {
        return orderService.bulkUpdateOrderStatus(request);
    }

    @Autowired
    private OrderService orderService;

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponseDto> getOrderById(@PathVariable Long orderId) {
        OrderResponseDto response = orderService.getOrderById(orderId);
        return ResponseEntity.ok(response);
    }
    /**
     * Get Order by ID (Admin Only)
     */
    @GetMapping("/admin/{orderId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminOrderResponseDto> getOrderByIdForAdmin(@PathVariable Long orderId) {
        AdminOrderResponseDto response = orderService.getOrderByAdminId(orderId);
        return ResponseEntity.ok(response);
    }
    /**
     * Update Order status (Admin Only)
     */
    @PatchMapping("/{orderId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderDTO> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestBody Map<String, String> request) {

        String status = request.get("status");
        OrderDTO response = orderService.updateOrderStatus(orderId, status);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Cancel Order (Customer and Admin )
     */
    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<OrderDTO> cancelOrder(
            @PathVariable Long orderId,
            @RequestParam Long userId,
            @RequestParam(defaultValue = "false") boolean isAdmin) {

        OrderDTO updatedOrder = orderService.cancelOrder(orderId, userId, isAdmin);
        return ResponseEntity.ok(updatedOrder);
    }

    @PatchMapping("/{orderId}/return")
    public ResponseEntity<OrderResponseDto> requestReturn(@PathVariable Long orderId) {
        OrderResponseDto response = orderService.requestReturn(orderId);
        return ResponseEntity.ok(response);
    }

}
