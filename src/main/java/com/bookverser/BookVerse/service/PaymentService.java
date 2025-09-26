package com.bookverser.BookVerse.service;

import com.bookverser.BookVerse.dto.PaymentDto;
import com.bookverser.BookVerse.dto.PaymentRequest;

public interface PaymentService {
    PaymentDto makePayment(PaymentRequest request);
}
