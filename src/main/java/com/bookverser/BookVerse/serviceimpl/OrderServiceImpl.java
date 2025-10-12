package com.bookverser.BookVerse.serviceimpl;

import com.bookverser.BookVerse.dto.*;
import com.bookverser.BookVerse.entity.*;
import com.bookverser.BookVerse.exception.*;
import com.bookverser.BookVerse.repository.*;
import com.bookverser.BookVerse.security.CustomUserDetails;
import com.bookverser.BookVerse.service.OrderService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final AddressRepository addressRepository;
    private final ModelMapper modelMapper;

    @Transactional
    @Override
    public OrderResponseDto placeOrder(PlaceOrderRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new UnauthorizedException("User not authenticated");
        }

        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        User customer = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new UnauthorizedException("Customer not found"));

        // Save Shipping Address
        Address shippingAddress = Address.builder()
                .city(request.getShippingAddress().getCity())
                .state(request.getShippingAddress().getState())
                .country(request.getShippingAddress().getCountry())
                .user(customer)
                .build();
        addressRepository.save(shippingAddress);

        // Validate payment method
        String method = request.getPaymentMethod().toUpperCase();
        if (!List.of("COD", "UPI", "CARD", "NET_BANKING").contains(method)) {
            throw new IllegalArgumentException("Invalid payment method: " + request.getPaymentMethod());
        }

        // Prepare order items
        BigDecimal totalPrice = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (PlaceOrderRequest.OrderItemRequest itemReq : request.getItems()) {
            Book book = bookRepository.findById(itemReq.getBookId())
                    .orElseThrow(() -> new BookNotFoundException("Book not found: " + itemReq.getBookId()));

            if (book.getStock() < itemReq.getQuantity()) {
                throw new InsufficientStockException("Insufficient stock for book: " + book.getTitle());
            }

            book.setStock(book.getStock() - itemReq.getQuantity());
            bookRepository.save(book);

            OrderItem orderItem = OrderItem.builder()
                    .book(book)
                    .seller(book.getSeller())
                    .quantity(itemReq.getQuantity())
                    .unitPrice(book.getPrice())
                    .build();

            totalPrice = totalPrice.add(book.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity())));
            orderItems.add(orderItem);
        }

        // Create Order
        Order order = Order.builder()
                .customer(customer)
                .shippingAddress(shippingAddress)
                .totalPrice(totalPrice)
                .status(Order.Status.PENDING)
                .paymentStatus(method.equals("COD") ? Order.PaymentStatus.COD : Order.PaymentStatus.PAID)
                .build();

        orderItems.forEach(item -> item.setOrder(order));
        order.setOrderItems(orderItems);
        Order savedOrder = orderRepository.save(order);

        // ================== Use ModelMapper ==================
        OrderResponseDto responseDto = modelMapper.map(savedOrder, OrderResponseDto.class);

        // Map nested fields manually if needed
        responseDto.setCustomerId(savedOrder.getCustomer().getId());
        responseDto.setPaymentMethod(request.getPaymentMethod());
        responseDto.setPaymentStatus(savedOrder.getPaymentStatus().name());
        responseDto.setItems(savedOrder.getOrderItems().stream()
                .map(item -> modelMapper.map(item, CartItemDto.class))
                .collect(Collectors.toList()));
        responseDto.setShippingAddress(modelMapper.map(savedOrder.getShippingAddress(), AddressResponseDto.class));

        return responseDto;
    }

    // ================== GET ORDER BY ID (CUSTOMER) ==================
    @Override
    public OrderResponseDto getOrderById(Long orderId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new UnauthorizedException("User not authenticated");
        }

        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));

        if (!order.getCustomer().getId().equals(userDetails.getId()) && !userDetails.isAdmin()) {
            throw new UnauthorizedException("Access denied");
        }

        return modelMapper.map(order, OrderResponseDto.class);
    }

    // ================== GET ORDER BY ID (ADMIN) ==================
    @Override
    @Transactional
    public AdminOrderResponseDto getOrderByAdminId(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));

        AdminOrderResponseDto response = modelMapper.map(order, AdminOrderResponseDto.class);
        response.setOrderId(order.getId());
        response.setBuyerId(order.getCustomer().getId());
        response.setBuyerName(order.getCustomer().getName());
        response.setBuyerEmail(order.getCustomer().getEmail());
        response.setTotalAmount(order.getTotalPrice());
        response.setItems(order.getOrderItems().stream()
                .map(item -> modelMapper.map(item, OrderDTO.class))
                .collect(Collectors.toList()));
        return response;
    }

    // ================== UPDATE ORDER STATUS ==================
    @Override
    @Transactional
    public OrderDTO updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));

        Order.Status newStatus;
        try {
            newStatus = Order.Status.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status. Allowed: PENDING, SHIPPED, DELIVERED, CANCELLED");
        }

        if (order.getStatus() == Order.Status.DELIVERED && newStatus != Order.Status.DELIVERED) {
            throw new IllegalArgumentException("Cannot change status of a delivered order");
        }

        order.setStatus(newStatus);
        Order updatedOrder = orderRepository.save(order);

        OrderDTO dto = modelMapper.map(updatedOrder, OrderDTO.class);
        dto.setBuyerId(updatedOrder.getCustomer().getId());
        if (!updatedOrder.getOrderItems().isEmpty()) {
            dto.setSellerId(updatedOrder.getOrderItems().get(0).getSeller().getId());
            dto.setBookId(updatedOrder.getOrderItems().get(0).getBook().getId());
        }
        return dto;
    }

    // ================== CANCEL ORDER ==================
    @Override
    public OrderDTO cancelOrder(Long orderId, Long userId, boolean isAdmin) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));

        if (!isAdmin && !Objects.equals(order.getCustomer().getId(), userId)) {
            throw new UnauthorizedException("You are not authorized to cancel this order.");
        }

        if (!(order.getStatus() == Order.Status.PENDING || order.getStatus() == Order.Status.CONFIRMED)) {
            throw new InvalidOrderStatusException("Order cannot be cancelled as it is already " + order.getStatus());
        }

        order.setStatus(Order.Status.CANCELLED);
        Order updatedOrder = orderRepository.save(order);
        return modelMapper.map(updatedOrder, OrderDTO.class);
    }

    // ================== GET MY ORDERS (CUSTOMER) ==================
    @Override
    public List<OrderResponseDto> getMyOrders(String email) {
        User customer = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Customer not found"));

        return orderRepository.findAll().stream()
                .filter(order -> order.getCustomer().getId().equals(customer.getId()))
                .map(order -> modelMapper.map(order, OrderResponseDto.class))
                .collect(Collectors.toList());
    }

    // ================== GET ALL ORDERS (ADMIN) ==================
    @Override
    public Page<OrderSummaryDto> getAllOrders(String status, LocalDate fromDate, LocalDate toDate,
                                              Long customerId, Pageable pageable) {
        List<OrderSummaryDto> filtered = orderRepository.findAll().stream()
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
                        order.getCreatedAt()))
                .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filtered.size());
        return new PageImpl<>(filtered.subList(start, end), pageable, filtered.size());
    }

    // ================== BULK UPDATE ORDER STATUS (ADMIN) ==================
    @Override
    @Transactional
    public List<OrderResponseDto> bulkUpdateOrderStatus(BulkOrderStatusUpdateRequest request) {
        if (request.getOrderIds() == null || request.getOrderIds().isEmpty()) {
            throw new IllegalArgumentException("Order IDs cannot be empty");
        }

        Order.Status targetStatus;
        try {
            targetStatus = Order.Status.valueOf(request.getStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid order status: " + request.getStatus());
        }

        List<Order> orders = orderRepository.findAllById(request.getOrderIds());
        if (orders.size() != request.getOrderIds().size()) {
            throw new RuntimeException("Some orders not found. Check the provided IDs.");
        }

        orders.forEach(order -> {
            if (order.getStatus() == Order.Status.CANCELLED) {
                throw new RuntimeException("Cannot update cancelled order: " + order.getId());
            }
            if (order.getStatus() == Order.Status.DELIVERED && targetStatus != Order.Status.DELIVERED) {
                throw new RuntimeException("Delivered order cannot be changed: " + order.getId());
            }
            order.setStatus(targetStatus);
        });

        orderRepository.saveAll(orders);

        return orders.stream().map(order -> modelMapper.map(order, OrderResponseDto.class)).collect(Collectors.toList());
    }
}
