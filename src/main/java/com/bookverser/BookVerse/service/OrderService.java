package com.bookverser.BookVerse.service;

import com.bookverser.BookVerse.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface OrderService {

    // CUSTOMER: Place order
    OrderResponseDto placeOrder(PlaceOrderRequest request);

    // CUSTOMER: Get single order
    OrderResponseDto getOrderById(Long orderId);

    // ADMIN: Get single order
    AdminOrderResponseDto getOrderByAdminId(Long orderId);

    // ADMIN: Update single order status
    OrderDTO updateOrderStatus(Long orderId, String status);

    // CUSTOMER or ADMIN: Cancel order
    OrderDTO cancelOrder(Long orderId, Long userId, boolean isAdmin);

    // CUSTOMER: Get all orders for logged-in user
    List<OrderResponseDto> getMyOrders(String email);

    // ADMIN: Get paginated and filtered orders
    Page<OrderSummaryDto> getAllOrders(String status, LocalDate fromDate, LocalDate toDate, Long customerId, Pageable pageable);

    // ADMIN: Bulk update order statuses
    List<OrderResponseDto> bulkUpdateOrderStatus(BulkOrderStatusUpdateRequest request);
}
