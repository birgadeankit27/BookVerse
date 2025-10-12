package com.bookverser.BookVerse.controller;

import com.bookverser.BookVerse.dto.*;
import com.bookverser.BookVerse.security.CustomUserDetails;
import com.bookverser.BookVerse.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // ================= CUSTOMER ENDPOINTS =================

    /**
     * Get all orders of the logged-in customer.
     */
    @GetMapping("/my")
    public List<OrderResponseDto> getMyOrders(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            throw new RuntimeException("Unauthorized: Please login");
        }
        return orderService.getMyOrders(userDetails.getUsername());
    }

    /**
     * Get a specific order by ID for a customer.
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponseDto> getOrderById(@PathVariable Long orderId) {
        OrderResponseDto response = orderService.getOrderById(orderId);
        return ResponseEntity.ok(response);
    }

    /**
     * Cancel an order (accessible by customer or admin).
     */
    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<OrderDTO> cancelOrder(
            @PathVariable Long orderId,
            @RequestParam Long userId,
            @RequestParam(defaultValue = "false") boolean isAdmin) {

        OrderDTO updatedOrder = orderService.cancelOrder(orderId, userId, isAdmin);
        return ResponseEntity.ok(updatedOrder);
    }

    // ================= ADMIN ENDPOINTS =================

    /**
     * Get all orders with optional filters (Admin only).
     */
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

    /**
     * Get order details by ID (Admin only).
     */
    @GetMapping("/admin/{orderId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminOrderResponseDto> getOrderByIdForAdmin(@PathVariable Long orderId) {
        AdminOrderResponseDto response = orderService.getOrderByAdminId(orderId);
        return ResponseEntity.ok(response);
    }

    /**
     * Update order status (Admin only).
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
     * Bulk update statuses for multiple orders (Admin only).
     */
    @PatchMapping("/admin/orders/status/bulk")
    @PreAuthorize("hasRole('ADMIN')")
    public List<OrderResponseDto> bulkUpdateOrderStatus(@RequestBody BulkOrderStatusUpdateRequest request) {
        return orderService.bulkUpdateOrderStatus(request);
    }
}
