package com.bookverser.BookVerse.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefundDto {
    private Long orderId;
    private String refundStatus;  // e.g., REFUNDED, MANUAL_REFUND_REQUIRED
    private String paymentMethod; // original payment method
}
