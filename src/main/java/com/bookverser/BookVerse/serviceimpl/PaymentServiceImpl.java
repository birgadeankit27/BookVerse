package com.bookverser.BookVerse.serviceimpl;

import org.springframework.stereotype.Service;

import com.bookverser.BookVerse.dto.PaymentDto;
import com.bookverser.BookVerse.dto.PaymentRequest;
import com.bookverser.BookVerse.entity.Order;
import com.bookverser.BookVerse.entity.Payment;
import com.bookverser.BookVerse.enums.PaymentMethod;
import com.bookverser.BookVerse.exception.InvalidPaymentMethodException;
import com.bookverser.BookVerse.exception.OrderNotFoundException;
import com.bookverser.BookVerse.exception.PaymentFailedException;
import com.bookverser.BookVerse.repository.OrderRepository;
import com.bookverser.BookVerse.repository.PaymentRepository;
import com.bookverser.BookVerse.service.PaymentService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService{


    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

	@Override
	public PaymentDto makePayment(PaymentRequest request) {
		// 1️⃣ Validate order
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));

        if (order.getStatus() != Order.Status.PENDING) {
            throw new PaymentFailedException("Order is not in PENDING state");
        }

        // 2️⃣ Validate payment method
        PaymentMethod method;
        try {
            method = PaymentMethod.valueOf(request.getPaymentMethod().toUpperCase());
        } catch (Exception e) {
            throw new InvalidPaymentMethodException("Invalid payment method");
        }

        // 3️⃣ Determine payment status
        Order.PaymentStatus paymentStatus;
        if (method == PaymentMethod.COD) {
            paymentStatus = Order.PaymentStatus.COD; // COD → pending until delivery
        } else {
            if (request.getTransactionId() == null || request.getTransactionId().isEmpty()) {
                throw new PaymentFailedException("Transaction ID required for online payments");
            }
            // Here you can integrate with actual payment gateway
            paymentStatus = Order.PaymentStatus.PAID;
        }

        // 4️⃣ Save payment
        Payment payment = Payment.builder()
                .order(order)
                .paymentMethod(method)
                .paymentStatus(paymentStatus)
                .transactionId(request.getTransactionId())
                .build();

        paymentRepository.save(payment);

        // 5️⃣ Update order payment status if not COD
        if (paymentStatus == Order.PaymentStatus.PAID) {
            order.setPaymentStatus(Order.PaymentStatus.PAID);
            orderRepository.save(order);
        }

        // 6️⃣ Return DTO
        return PaymentDto.builder()
                .paymentId(payment.getId())
                .orderId(order.getId())
                .paymentMethod(payment.getPaymentMethod())
                .paymentStatus(payment.getPaymentStatus())
                .transactionId(payment.getTransactionId())
                .build();
    }
	

}
