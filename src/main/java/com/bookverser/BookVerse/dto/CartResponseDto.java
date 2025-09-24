package com.bookverser.BookVerse.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartResponseDto {
    private Long cartId;
    private Long customerId;
    private List<CartItemDto> items;
    private BigDecimal totalPrice;  // ✅ Changed from double → BigDecimal
}
