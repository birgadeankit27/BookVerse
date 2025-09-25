package com.bookverser.BookVerse.dto;

import com.bookverser.BookVerse.entity.Cart;
import com.bookverser.BookVerse.entity.CartItem;
import com.bookverser.BookVerse.entity.Book; // Added import for Book if needed for getTitle/getAuthor
import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartResponseDto {
    private Long cartId;
    private Long buyerId;
    private List<CartItemDto> items;
    private BigDecimal totalPrice;

    public static CartResponseDto fromEntity(Cart cart) {
        return CartResponseDto.builder()
                .cartId(cart.getId())
                .buyerId(cart.getCustomer().getId()) // Fixed: Changed getBuyer() to getCustomer()
                .totalPrice(cart.getTotalPrice())
                .items(cart.getCartItems().stream()
                        .map(CartResponseDto::toCartItemDto)
                        .collect(Collectors.toList()))
                .build();
    }

    private static CartItemDto toCartItemDto(CartItem item) {
        BigDecimal subtotal = item.getPrice()
                .multiply(BigDecimal.valueOf(item.getQuantity()));

        return CartItemDto.builder()
                .id(item.getId())
                .bookId(item.getBook().getId())
                .title(item.getBook().getTitle())
                .author(item.getBook().getAuthor())
                .price(item.getPrice())
                .quantity(item.getQuantity())
                .subtotal(subtotal)
                .build();
    }
}