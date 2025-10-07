package com.bookverser.BookVerse.dto;

import com.bookverser.BookVerse.enums.PaymentMethod;
import com.bookverser.BookVerse.entity.Order;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentSummaryDto {
    private Long paymentId;
    private Long orderId;
    private String email;
    private PaymentMethod paymentMethod;
    private Order.PaymentStatus paymentStatus;
    private String transactionId;
    private LocalDateTime createdAt;
}
