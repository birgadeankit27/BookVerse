package com.bookverser.BookVerse.controller;

import com.bookverser.BookVerse.dto.AdminOrderResponseDto;
import com.bookverser.BookVerse.dto.OrderResponseDto;
import com.bookverser.BookVerse.dto.PlaceOrderRequest;
import com.bookverser.BookVerse.entity.Order;
import com.bookverser.BookVerse.exception.OrderNotFoundException;
import com.bookverser.BookVerse.exception.UnauthorizedException;
import com.bookverser.BookVerse.repository.OrderRepository;
import com.bookverser.BookVerse.security.CustomUserDetails;
import com.bookverser.BookVerse.service.OrderService;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

	@Autowired
	private OrderService orderService;

	@Autowired
	private OrderRepository orderRepository;

	@GetMapping("/{orderId}")
	public ResponseEntity<OrderResponseDto> getOrderById(@PathVariable Long orderId) {
		OrderResponseDto response = orderService.getOrderById(orderId);
		return ResponseEntity.ok(response);
	}

	/**
	 * Get Order by ID (Admin Only)
	 */
	@GetMapping("/admin/{orderId}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<AdminOrderResponseDto> getOrderByIdForAdmin(@PathVariable Long orderId) {
		AdminOrderResponseDto response = orderService.getOrderByAdminId(orderId);
		return ResponseEntity.ok(response);
	}

	@PatchMapping("/{orderId}/return")
	public ResponseEntity<OrderResponseDto> requestReturn(@PathVariable Long orderId) {
		OrderResponseDto response = orderService.requestReturn(orderId);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/{orderId}/invoice")
	public ResponseEntity<byte[]> generateInvoicePdf(@PathVariable Long orderId) {
		// Generate invoice PDF
		byte[] pdfBytes = orderService.generateInvoicePdf(orderId);
		
		// Return the file as a downloadable PDF
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_PDF);
		headers.setContentDisposition(ContentDisposition.attachment().filename("invoice_" + orderId + ".pdf").build());

		return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
	}
}
