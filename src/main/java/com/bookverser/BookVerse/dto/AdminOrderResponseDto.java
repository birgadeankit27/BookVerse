package com.bookverser.BookVerse.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminOrderResponseDto {
    private Long orderId;
    private Long buyerId;
    private String buyerName;
    private String buyerEmail;
    private String status;
    private String paymentStatus;
    private BigDecimal totalAmount;
    private List<OrderDTO> items;
    private AddressResponseDto shippingAddress;
}
