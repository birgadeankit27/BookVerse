package com.bookverser.BookVerse.controller;

import com.bookverser.BookVerse.dto.PaymentDto;
import com.bookverser.BookVerse.dto.PaymentRequest;
import com.bookverser.BookVerse.dto.PaymentSummaryDto;
import com.bookverser.BookVerse.dto.RefundDto;
import com.bookverser.BookVerse.entity.Order;
import com.bookverser.BookVerse.entity.User;
import com.bookverser.BookVerse.repository.UserRepository;
import com.bookverser.BookVerse.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final UserRepository userRepository;

    // ==================== Make Payment ====================
    @PostMapping("/makePayment")
    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER')") // Only customers can pay
    public ResponseEntity<PaymentDto> makePayment(
            Authentication authentication,
            @Valid @RequestBody PaymentRequest request
    ) {
        // 1️⃣ Get logged-in user
        String email = authentication.getName();
        User customer = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        // 2️⃣ Optional: you can check if the order belongs to this customer
        // if(!orderRepository.findById(request.getOrderId())
        //         .map(order -> order.getCustomer().getId().equals(customer.getId()))
        //         .orElse(false)) {
        //     throw new RuntimeException("Order does not belong to this customer");
        // }

        // 3️⃣ Call service
        PaymentDto dto = paymentService.makePayment(request);

        // 4️⃣ Return response
        return ResponseEntity.status(201).body(dto);
    }
    
 // ==================== Get Payment by ID ====================
    
    @GetMapping("/getPayment/{paymentId}")
    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER', 'ROLE_ADMIN')") // Only buyers & admins
    public ResponseEntity<PaymentDto> getPaymentById(
            Authentication authentication,
            @PathVariable Long paymentId
    ) {
        String email = authentication.getName();
        PaymentDto dto = paymentService.getPaymentById(paymentId, email);
        return ResponseEntity.ok(dto);
    }
    
    
    // ==================== Get My Transactions ====================
    
    @GetMapping("/my")
    @PreAuthorize("hasAuthority('ROLE_CUSTOMER')") // Only buyers can access
    public ResponseEntity<List<PaymentDto>> getMyTransactions(Authentication authentication) {
    	
        String email = authentication.getName(); // Get logged-in user's email
        
        List<PaymentDto> transactions = paymentService.getMyTransactions(email);
        
        return ResponseEntity.ok(transactions);
    }
    
    // ==================== PROCESS-REFUND ====================
    @PostMapping("/{orderId}/refund")
    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER', 'ROLE_ADMIN')")
    public ResponseEntity<RefundDto> processRefund(
            Authentication authentication,
            @PathVariable Long orderId
    ) {
        String email = authentication.getName();
        RefundDto dto = paymentService.processRefund(orderId, email);
        return ResponseEntity.ok(dto);
    }
    
    
 // ==================== PAYMENT:ADMIN:GET-ALL ====================
    
    @GetMapping("/admin/getAll")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")


    public ResponseEntity<Page<PaymentSummaryDto>> getAllPayments(
            @RequestParam(required = false) Order.PaymentStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @RequestParam(required = false) String email,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<PaymentSummaryDto> result = paymentService.getAllPayments(status, fromDate, toDate, email, page, size);
        return ResponseEntity.ok(result);
    }
}
