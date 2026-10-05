//package com.bookverser.BookVerse.service;
//import java.time.LocalDate;
//import java.util.List;
//import org.hibernate.query.Page;
//import com.bookverser.BookVerse.dto.AdminOrderResponseDto;
//import com.bookverser.BookVerse.dto.BulkOrderStatusUpdateRequest;
//import com.bookverser.BookVerse.dto.OrderDTO;
//import com.bookverser.BookVerse.dto.OrderResponseDto;
//import com.bookverser.BookVerse.dto.PlaceOrderRequest;
//
//public interface OrderService {
//
//    List<OrderResponseDto> getMyOrders(String email);
//
//    Page<OrderSummaryDto> getAllOrders(
//            String status,
//            LocalDate fromDate,
//            LocalDate toDate,
//            Long customerId,
//            Pageable pageable
//    );
//
//    List<OrderResponseDto> bulkUpdateOrderStatus(
//            BulkOrderStatusUpdateRequest request
//    );
//
//    OrderResponseDto placeOrder(PlaceOrderRequest request);
//
//    OrderResponseDto getOrderById(Long orderId);
//
//    AdminOrderResponseDto getOrderByAdminId(Long orderId);
//
//    OrderDTO updateOrderStatus(Long orderId, String status);
//
//    OrderDTO cancelOrder(Long orderId, Long userId, boolean isAdmin);
//
//    OrderResponseDto requestReturn(Long orderId);
//
//    byte[] generateInvoicePdf(Long orderId);
//}





package com.bookverser.BookVerse.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.bookverser.BookVerse.dto.AdminOrderResponseDto;
import com.bookverser.BookVerse.dto.BulkOrderStatusUpdateRequest;
import com.bookverser.BookVerse.dto.OrderDTO;
import com.bookverser.BookVerse.dto.OrderResponseDto;
import com.bookverser.BookVerse.dto.OrderSummaryDto;
import com.bookverser.BookVerse.dto.PlaceOrderRequest;

public interface OrderService {

    List<OrderResponseDto> getMyOrders(String email);

    Page<OrderSummaryDto> getAllOrders(
            String status,
            LocalDate fromDate,
            LocalDate toDate,
            Long customerId,
            Pageable pageable
    );

    List<OrderResponseDto> bulkUpdateOrderStatus(
            BulkOrderStatusUpdateRequest request
    );

    OrderResponseDto placeOrder(PlaceOrderRequest request);

    OrderResponseDto getOrderById(Long orderId);

    AdminOrderResponseDto getOrderByAdminId(Long orderId);

    OrderDTO updateOrderStatus(Long orderId, String status);

    OrderDTO cancelOrder(Long orderId, Long userId, boolean isAdmin);

    OrderResponseDto requestReturn(Long orderId);

    byte[] generateInvoicePdf(Long orderId);
}