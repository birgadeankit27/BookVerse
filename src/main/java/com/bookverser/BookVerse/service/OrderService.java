package com.bookverser.BookVerse.service;
import com.bookverser.BookVerse.dto.OrderResponseDto;
import com.bookverser.BookVerse.dto.OrderSummaryDto;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.bookverser.BookVerse.dto.AdminOrderResponseDto;
import com.bookverser.BookVerse.dto.OrderDTO;
import com.bookverser.BookVerse.dto.PlaceOrderRequest;

public interface OrderService {
	public OrderResponseDto placeOrder(PlaceOrderRequest request);
	public OrderResponseDto getOrderById(Long orderId);
	List<OrderResponseDto> getMyOrders(String email);
	Page<OrderSummaryDto> getAllOrders(String status, LocalDate fromDate, LocalDate toDate, Long customerId, Pageable pageable);
	public  AdminOrderResponseDto getOrderByAdminId(Long orderId);
	 OrderDTO updateOrderStatus(Long orderId, String status);

}
