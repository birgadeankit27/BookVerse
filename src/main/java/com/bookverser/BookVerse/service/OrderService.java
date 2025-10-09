package com.bookverser.BookVerse.service;


import com.bookverser.BookVerse.dto.BulkOrderStatusUpdateRequest;

import com.bookverser.BookVerse.dto.OrderResponseDto;
import com.bookverser.BookVerse.dto.OrderSummaryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface OrderService {
    // Customer
    List<OrderResponseDto> getMyOrders(String email);

    // Admin
    Page<OrderSummaryDto> getAllOrders(String status,
                                       LocalDate fromDate,
                                       LocalDate toDate,
                                       Long customerId,
                                       Pageable pageable);

    
    List<OrderResponseDto> bulkUpdateOrderStatus(BulkOrderStatusUpdateRequest request);


import com.bookverser.BookVerse.dto.AdminOrderResponseDto;
import com.bookverser.BookVerse.dto.OrderDTO;
import com.bookverser.BookVerse.dto.OrderResponseDto;
import com.bookverser.BookVerse.dto.PlaceOrderRequest;

public interface OrderService {
	public OrderResponseDto placeOrder(PlaceOrderRequest request);
	public OrderResponseDto getOrderById(Long orderId);
	public  AdminOrderResponseDto getOrderByAdminId(Long orderId);
	public OrderResponseDto requestReturn(Long orderId);
	 OrderDTO updateOrderStatus(Long orderId, String status);

	 OrderDTO cancelOrder(Long orderId, Long userId, boolean isAdmin);


}
