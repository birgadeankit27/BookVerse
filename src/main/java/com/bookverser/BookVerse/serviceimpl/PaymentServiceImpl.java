package com.bookverser.BookVerse.serviceimpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.bookverser.BookVerse.dto.PaymentDto;
import com.bookverser.BookVerse.dto.PaymentRequest;
import com.bookverser.BookVerse.dto.PaymentSummaryDto;
import com.bookverser.BookVerse.dto.RefundDto;
import com.bookverser.BookVerse.entity.Order;
import com.bookverser.BookVerse.entity.Order.PaymentStatus;
import com.bookverser.BookVerse.entity.Payment;
import com.bookverser.BookVerse.enums.PaymentMethod;
import com.bookverser.BookVerse.exception.InvalidPaymentMethodException;
import com.bookverser.BookVerse.exception.OrderNotFoundException;
import com.bookverser.BookVerse.exception.PaymentFailedException;
import com.bookverser.BookVerse.exception.PaymentNotFoundException;
import com.bookverser.BookVerse.exception.RefundNotAllowedException;
import com.bookverser.BookVerse.exception.UnauthorizedException;
import com.bookverser.BookVerse.repository.OrderRepository;
import com.bookverser.BookVerse.repository.PaymentRepository;
import com.bookverser.BookVerse.repository.UserRepository;
import com.bookverser.BookVerse.service.PaymentService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService{


    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

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

	@Override
	public PaymentDto getPaymentById(Long paymentId, String userEmail) {
		  Payment payment = paymentRepository.findById(paymentId)
	                .orElseThrow(() -> new PaymentNotFoundException("Payment not found"));

	        // Get buyer email from the payment's order
	        String buyerEmail = payment.getOrder().getCustomer().getEmail();

	        // Check if the logged-in user is the buyer or admin
	        boolean isAdmin = userRepository.findByEmail(userEmail)
	                .map(user -> user.getRoles().stream().anyMatch(role -> role.getName().equals("ADMIN")))
	                .orElse(false);

	        if (!userEmail.equals(buyerEmail) && !isAdmin) {
	            throw new UnauthorizedException("You are not allowed to view this payment");
	        }

	        // Return DTO
	        return PaymentDto.builder()
	                .paymentId(payment.getId())
	                .orderId(payment.getOrder().getId())
	                .paymentMethod(payment.getPaymentMethod())
	                .paymentStatus(payment.getPaymentStatus())
	                .transactionId(payment.getTransactionId())
	                .build();
	}

	@Override
	public List<PaymentDto> getMyTransactions(String userEmail) {
		// 1️⃣ Find the logged-in user
        Long customerId = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();

        // 2️⃣ Fetch all payments for this customer
        List<Payment> payments = paymentRepository.findByOrder_Customer_Id(customerId);

        // 3️⃣ Convert to PaymentDto
        return payments.stream()
                .map(payment -> PaymentDto.builder()
                        .paymentId(payment.getId())
                        .orderId(payment.getOrder().getId())
                        .paymentMethod(payment.getPaymentMethod())
                        .paymentStatus(payment.getPaymentStatus())
                        .transactionId(payment.getTransactionId())
                        .build())
                .collect(Collectors.toList());
	}

	@Override
	public RefundDto processRefund(Long orderId, String userEmail) {
		 // 1️⃣ Find the order
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));

        // 2️⃣ Check if logged-in user is buyer or admin
        boolean isAdmin = userRepository.findByEmail(userEmail)
                .map(user -> user.getRoles().stream().anyMatch(role -> role.getName().equals("ADMIN")))
                .orElse(false);

        if (!isAdmin && !order.getCustomer().getEmail().equals(userEmail)) {
            throw new UnauthorizedException("You are not allowed to process this refund");
        }

        // 3️⃣ Check if order is eligible for refund
        if (!(order.getStatus() == Order.Status.CANCELLED || order.getStatus() == Order.Status.DELIVERED)) {
            throw new RefundNotAllowedException("Order is not eligible for refund");
        }

        // 4️⃣ Get payment
        Payment payment = paymentRepository.findByOrder_Id(orderId)
                .orElseThrow(() -> new RefundNotAllowedException("Payment not found for this order"));

        // 5️⃣ Process refund
        String refundStatus;
        if (payment.getPaymentMethod().name().equals("COD")) {
            refundStatus = "MANUAL_REFUND_REQUIRED";
            payment.setPaymentStatus(Order.PaymentStatus.MANUAL_REFUND_REQUIRED);
        } else {
            // Here you would integrate with payment gateway to refund
            refundStatus = "REFUNDED";
            payment.setPaymentStatus(Order.PaymentStatus.REFUNDED);
        }

        paymentRepository.save(payment);

        // 6️⃣ Return RefundDto
        return RefundDto.builder()
                .orderId(order.getId())
                .paymentMethod(payment.getPaymentMethod().name())
                .refundStatus(refundStatus)
                .build();
	}

	@Override
	public Page<PaymentSummaryDto> getAllPayments(Order.PaymentStatus status,LocalDateTime fromDate,LocalDateTime toDate, String email, int page, int size) 
			 {
		 Page<Payment> payments = paymentRepository.findAllWithFilters(status, fromDate, toDate, email, PageRequest.of(page, size));

	        return payments.map(p -> PaymentSummaryDto.builder()
	                .paymentId(p.getId())
	                .orderId(p.getOrder().getId())
	                .email(p.getOrder().getCustomer().getEmail())
	                .paymentMethod(p.getPaymentMethod())
	                .paymentStatus(p.getPaymentStatus())
	                .transactionId(p.getTransactionId())
	                .createdAt(p.getOrder().getCreatedAt())
	                .build()
	        );
	}
	

}
