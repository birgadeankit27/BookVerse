// OrderResponseDto.java
package com.bookverser.BookVerse.dto;

import lombok.Data;
import java.util.List;

import com.bookverser.BookVerse.dto.PlaceOrderRequest.AddressRequest;

@Data
public class OrderResponseDto {
    private Long orderId;
    private Long customerId;
    private String paymentMethod;
    private String status;
    private double totalAmount;
    private List<CartItemDto> items;
    private AddressResponseDto shippingAddress;
}
