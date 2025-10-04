package com.bookverser.BookVerse.serviceimpl;

import com.bookverser.BookVerse.dto.BulkOrderStatusUpdateRequest;
import com.bookverser.BookVerse.dto.CartItemDto;
import com.bookverser.BookVerse.dto.OrderResponseDto;
import com.bookverser.BookVerse.dto.OrderSummaryDto;
import com.bookverser.BookVerse.entity.Order;
import com.bookverser.BookVerse.entity.OrderItem;
import com.bookverser.BookVerse.entity.User;
import com.bookverser.BookVerse.exception.UnauthorizedException;
import com.bookverser.BookVerse.repository.OrderRepository;
import com.bookverser.BookVerse.repository.UserRepository;
import com.bookverser.BookVerse.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    // ================== CUSTOMER: GET MY ORDERS ==================
    @Override
    public List<OrderResponseDto> getMyOrders(String email) {
        User customer = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Customer not found"));

        List<Order> orders = orderRepository.findAll()
                .stream()
                .filter(order -> order.getCustomer().getId().equals(customer.getId()))
                .collect(Collectors.toList());

        return orders.stream().map(order -> {
            OrderResponseDto dto = new OrderResponseDto();
            dto.setOrderId(order.getId());
            dto.setCustomerId(order.getCustomer().getId());
            dto.setPaymentMethod(order.getPaymentStatus().name());
            dto.setStatus(order.getStatus().name());
            dto.setTotalAmount(order.getTotalPrice());

            List<CartItemDto> items = order.getOrderItems().stream().map(item -> {
                CartItemDto cartItemDto = new CartItemDto();
                cartItemDto.setBookId(item.getBook().getId());
                cartItemDto.setQuantity(item.getQuantity());
                cartItemDto.setPrice(item.getUnitPrice());
                return cartItemDto;
            }).collect(Collectors.toList());

            dto.setItems(items);
            return dto;
        }).collect(Collectors.toList());
    }

    // ================== ADMIN: GET ALL ORDERS ==================
    @Override
    public Page<OrderSummaryDto> getAllOrders(String status,
                                              LocalDate fromDate,
                                              LocalDate toDate,
                                              Long customerId,
                                              Pageable pageable) {

        List<Order> orders = orderRepository.findAll();

        List<OrderSummaryDto> filtered = orders.stream()
                .filter(order -> status == null || order.getStatus().name().equalsIgnoreCase(status))
                .filter(order -> fromDate == null || !order.getCreatedAt().toLocalDate().isBefore(fromDate))
                .filter(order -> toDate == null || !order.getCreatedAt().toLocalDate().isAfter(toDate))
                .filter(order -> customerId == null || order.getCustomer().getId().equals(customerId))
                .map(order -> new OrderSummaryDto(
                        order.getId(),
                        order.getCustomer().getId(),
                        order.getCustomer().getEmail(),
                        order.getStatus().name(),
                        order.getPaymentStatus().name(),
                        order.getTotalPrice(),
                        order.getCreatedAt()
                ))
                .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filtered.size());
        List<OrderSummaryDto> pagedList = filtered.subList(start, end);

        return new PageImpl<>(pagedList, pageable, filtered.size());
    }

    @Override
    public List<OrderResponseDto> bulkUpdateOrderStatus(BulkOrderStatusUpdateRequest request) {
        List<Long> orderIds = request.getOrderIds();
        String newStatus = request.getStatus().toUpperCase();

        // ✅ Validate input
        if (orderIds == null || orderIds.isEmpty()) {
            throw new IllegalArgumentException("Order IDs cannot be empty");
        }

        // ✅ Validate status
        Order.Status targetStatus;
        try {
            targetStatus = Order.Status.valueOf(newStatus);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid order status: " + newStatus);
        }

        // ✅ Fetch orders
        List<Order> orders = orderRepository.findAllById(orderIds);
        if (orders.size() != orderIds.size()) {
            throw new RuntimeException("Some orders not found. Check the provided IDs.");
        }

        // ✅ Validate allowed status transitions (optional rule enforcement)
        for (Order order : orders) {
            if (order.getStatus() == Order.Status.CANCELLED) {
                throw new RuntimeException("Cannot update cancelled orders: Order ID " + order.getId());
            }
            // Example: prevent reverting delivered orders
            if (order.getStatus() == Order.Status.DELIVERED && targetStatus != Order.Status.DELIVERED) {
                throw new RuntimeException("Delivered order cannot be changed: Order ID " + order.getId());
            }
        }

        // ✅ Update status
        orders.forEach(order -> order.setStatus(targetStatus));
        orderRepository.saveAll(orders);

        // ✅ Return response DTOs
        return orders.stream().map(order -> {
            OrderResponseDto dto = new OrderResponseDto();
            dto.setOrderId(order.getId());
            dto.setCustomerId(order.getCustomer().getId());
            dto.setPaymentMethod(order.getPaymentStatus().name());
            dto.setStatus(order.getStatus().name());
            dto.setTotalAmount(order.getTotalPrice());

            List<CartItemDto> items = order.getOrderItems().stream().map(item -> {
                CartItemDto cartItemDto = new CartItemDto();
                cartItemDto.setBookId(item.getBook().getId());
                cartItemDto.setQuantity(item.getQuantity());
                cartItemDto.setPrice(item.getUnitPrice());
                return cartItemDto;
            }).collect(Collectors.toList());

            dto.setItems(items);
            return dto;
        }).collect(Collectors.toList());
    }

}
