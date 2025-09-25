package com.bookverser.BookVerse.controller;

import com.bookverser.BookVerse.dto.CartResponseDto;
import com.bookverser.BookVerse.security.CustomUserDetails;
import com.bookverser.BookVerse.service.CartService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    /**
     * Adds a book to the buyer's cart.
     * @param buyerId ID of the buyer.
     * @param request AddToCartRequest containing bookId and quantity.
     * @return CartResponseDto with updated cart details.
     */

    
   
    
//    geting cart item 
    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CartResponseDto> getCartItems(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long buyerId = userDetails.getId(); // Token मधून id मिळतं
        CartResponseDto response = cartService.getCartItems(buyerId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
