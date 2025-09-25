package com.bookverser.BookVerse.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.bookverser.BookVerse.dto.AddToCartRequest;
import com.bookverser.BookVerse.dto.CartResponseDto;
import com.bookverser.BookVerse.dto.CheckoutRequest;
import com.bookverser.BookVerse.dto.UpdateCartRequest;
import com.bookverser.BookVerse.entity.User;
import com.bookverser.BookVerse.exception.UnauthorizedException;
import com.bookverser.BookVerse.repository.UserRepository;
import com.bookverser.BookVerse.service.CartService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final UserRepository userRepository;

    // ====================  Add a Book to Cart ====================
    
    @PostMapping("/add")
    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER')")
// must match JWT authorities
    public ResponseEntity<CartResponseDto> addToCart(
            Authentication authentication,
            @Valid @RequestBody AddToCartRequest request
    ) {
        String email = authentication.getName(); // get logged-in user
        User customer = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        CartResponseDto response = cartService.addToCart(customer.getId(), request);
        return ResponseEntity.ok(response);
    }
    
 // --------------------------Remove Book from Cart-------------------------
    @DeleteMapping("/{bookId}")
    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER')")
    public ResponseEntity<Map<String, Object>> removeCartItem(
            Authentication authentication,
            @PathVariable Long bookId
    ) {
        // 🔹 Get logged-in user email from JWT
        String email = authentication.getName();
        User customer = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Unauthorized: User not found"));

        // 🔹 Call service to remove cart item
        Map<String, Object> response = cartService.removeCartItem(customer.getId(), bookId);

        return ResponseEntity.ok(response); // ✅ 200 OK with updated cart
    }

    
   

}
