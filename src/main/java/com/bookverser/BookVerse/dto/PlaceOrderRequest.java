package com.bookverser.BookVerse.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class PlaceOrderRequest {

    @NotNull(message = "Shipping address is required")
    private String shippingAddress;  // ✅ String instead of ID

    @NotNull(message = "Payment method is required")
    private String paymentMethod;  // COD, UPI, Card, NetBanking

    @NotEmpty(message = "Order must contain at least one item")
    private List<OrderItemRequest> items;

    private String couponCode;  // optional

    @Data
    public static class OrderItemRequest {
        @NotNull(message = "Book ID is required")
        private Long bookId;

        @NotNull(message = "Quantity is required")
        private Integer quantity;
    }
}
