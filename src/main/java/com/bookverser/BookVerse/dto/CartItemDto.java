package com.bookverser.BookVerse.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemDto {
    private Long id;          // CartItem ID
    private Long bookId;
    private String author;
    private String title;
    private int quantity;
    private BigDecimal price;   // per unit (BigDecimal for money values)
    private BigDecimal total;   // price * quantity (BigDecimal)
}

