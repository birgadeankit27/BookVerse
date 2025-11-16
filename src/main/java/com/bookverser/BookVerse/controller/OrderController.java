package com.bookverser.BookVerse.controller;
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
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;



@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")

@RequiredArgsConstructor
public class OrderController {


    
    @Autowired
    private OrderService orderService;


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



}
