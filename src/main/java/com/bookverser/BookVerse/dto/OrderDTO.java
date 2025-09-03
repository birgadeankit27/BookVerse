package com.bookverser.BookVerse.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDTO {
	
	 private Long id;
	    private Long buyerId;
	    private Long sellerId;
	    private Long bookId;
	    private String status;        // PENDING, SHIPPED, DELIVERED
	    private String paymentStatus; // PAID, COD

}
