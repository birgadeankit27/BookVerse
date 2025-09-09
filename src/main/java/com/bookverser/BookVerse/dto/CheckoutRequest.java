package com.bookverser.BookVerse.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CheckoutRequest {
    @NotBlank(message = "Payment method is required")
    private String paymentMethod; // e.g., COD, UPI, CARD
}
