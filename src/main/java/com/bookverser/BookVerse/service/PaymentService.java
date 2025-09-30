package com.bookverser.BookVerse.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;

import com.bookverser.BookVerse.dto.PaymentDto;
import com.bookverser.BookVerse.dto.PaymentRequest;
import com.bookverser.BookVerse.dto.PaymentSummaryDto;
import com.bookverser.BookVerse.dto.RefundDto;
import com.bookverser.BookVerse.entity.Order;

public interface PaymentService {
    PaymentDto makePayment(PaymentRequest request);
    
    PaymentDto getPaymentById(Long paymentId, String userEmail); 
    
    // New method for fetching logged-in buyer transactions
    List<PaymentDto> getMyTransactions(String userEmail);
    RefundDto processRefund(Long orderId, String userEmail);
    
    Page<PaymentSummaryDto> getAllPayments(
            Order.PaymentStatus status,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            String email,
            int page,
            int size
    );
}
