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
}
