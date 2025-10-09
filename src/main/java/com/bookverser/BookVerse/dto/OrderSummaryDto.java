package com.bookverser.BookVerse.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderSummaryDto {
    private Long orderId;
    private Long customerId;
    private String customerEmail;
    private String status;
    private String paymentStatus;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
}
