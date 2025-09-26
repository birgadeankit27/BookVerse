package com.bookverser.BookVerse.controller;

import com.bookverser.BookVerse.dto.PaymentDto;
import com.bookverser.BookVerse.dto.PaymentRequest;
import com.bookverser.BookVerse.entity.User;
import com.bookverser.BookVerse.repository.UserRepository;
import com.bookverser.BookVerse.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
}
