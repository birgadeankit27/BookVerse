package com.bookverser.BookVerse.dto;

import com.bookverser.BookVerse.enums.PaymentMethod;
import com.bookverser.BookVerse.entity.Order;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentDto {
    private Long paymentId;
    private Long orderId;
    private PaymentMethod paymentMethod;
    private Order.PaymentStatus paymentStatus;
    private String transactionId;
}
