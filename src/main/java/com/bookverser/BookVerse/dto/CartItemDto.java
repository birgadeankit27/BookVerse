package com.bookverser.BookVerse.dto;

import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemDto {
    private Long id;          // CartItem ID
    private Long bookId;
    private String title;
    private String author;
    private BigDecimal price;
    private int quantity;
    private BigDecimal subtotal;
}
