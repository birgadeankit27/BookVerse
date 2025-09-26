package com.bookverser.BookVerse.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {
    private Long orderId;
    private String paymentMethod; // COD, UPI, CARD, NET_BANKING
    private String transactionId; // Required for online payments
}
