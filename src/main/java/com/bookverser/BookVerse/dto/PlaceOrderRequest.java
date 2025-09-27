package com.bookverser.BookVerse.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class PlaceOrderRequest {

	@NotNull
	private String paymentMethod;

	@NotEmpty
	private List<OrderItemRequest> items;

	@NotNull
	private AddressRequest shippingAddress;

	@Data
	public static class OrderItemRequest {
		@NotNull
		private Long bookId;

		@NotNull
		private Integer quantity;
	}

	@Data
	public static class AddressRequest {
		@NotBlank
		private String city;

		@NotBlank
		private String state;

		@NotBlank
		private String country;
	}
}
