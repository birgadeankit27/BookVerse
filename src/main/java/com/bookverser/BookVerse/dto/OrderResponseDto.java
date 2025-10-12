package com.bookverser.BookVerse.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponseDto {
    private Long orderId;
    private Long customerId;
    private String paymentMethod;
    private String paymentStatus;
    private String status;
    private BigDecimal totalAmount;
    private List<CartItemDto> items;
    private AddressResponseDto shippingAddress;
}
