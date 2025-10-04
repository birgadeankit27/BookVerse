package com.bookverser.BookVerse.serviceimpl;

import com.bookverser.BookVerse.dto.CartItemDto;
import com.bookverser.BookVerse.dto.OrderResponseDto;
import com.bookverser.BookVerse.dto.OrderSummaryDto;
import com.bookverser.BookVerse.entity.Order;
import com.bookverser.BookVerse.entity.OrderItem;
import com.bookverser.BookVerse.entity.User;
import com.bookverser.BookVerse.exception.UnauthorizedException;
import com.bookverser.BookVerse.repository.OrderRepository;
import com.bookverser.BookVerse.repository.UserRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.bookverser.BookVerse.dto.CartItemDto;
import com.bookverser.BookVerse.dto.OrderDTO;
import com.bookverser.BookVerse.dto.OrderResponseDto;
import com.bookverser.BookVerse.dto.PlaceOrderRequest;
import com.bookverser.BookVerse.dto.AddressResponseDto;
import com.bookverser.BookVerse.dto.AdminOrderResponseDto;
import com.bookverser.BookVerse.entity.Address;
import com.bookverser.BookVerse.entity.Book;
import com.bookverser.BookVerse.entity.Order;
import com.bookverser.BookVerse.entity.OrderItem;
import com.bookverser.BookVerse.entity.User;
import com.bookverser.BookVerse.exception.BookNotFoundException;
import com.bookverser.BookVerse.exception.InsufficientStockException;
import com.bookverser.BookVerse.exception.OrderNotFoundException;
import com.bookverser.BookVerse.exception.UnauthorizedException;
import com.bookverser.BookVerse.repository.AddressRepository;
import com.bookverser.BookVerse.repository.BookRepository;
import com.bookverser.BookVerse.repository.OrderRepository;
import com.bookverser.BookVerse.repository.UserRepository;
import com.bookverser.BookVerse.security.CustomUserDetails;

import com.bookverser.BookVerse.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;


import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.transaction.Transactional;


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

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private AddressRepository addressRepository;
    
    @Autowired
    private ModelMapper modelMapper;

    @Transactional
    @Override
    public OrderResponseDto placeOrder(PlaceOrderRequest request) {
        // 1️⃣ Get authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new UnauthorizedException("User not authenticated");
        }

        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        User customer = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new UnauthorizedException("Customer not found"));

        // 2️⃣ Save new shipping address
        Address shippingAddress = Address.builder()
                .city(request.getShippingAddress().getCity())
                .state(request.getShippingAddress().getState())
                .country(request.getShippingAddress().getCountry())
                .user(customer)
                .build();

        shippingAddress = addressRepository.save(shippingAddress);

        // 3️⃣ Validate payment method
        if (!List.of("COD", "UPI", "CARD", "NET_BANKING").contains(request.getPaymentMethod().toUpperCase())) {
            throw new IllegalArgumentException("Invalid payment method: " + request.getPaymentMethod());
        }

        // 4️⃣ Validate items & calculate total
        BigDecimal totalPrice = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (PlaceOrderRequest.OrderItemRequest itemReq : request.getItems()) {
            Book book = bookRepository.findById(itemReq.getBookId())
                    .orElseThrow(() -> new BookNotFoundException("Book not found: " + itemReq.getBookId()));

            if (book.getStock() < itemReq.getQuantity()) {
                throw new InsufficientStockException("Book " + book.getTitle() + " has insufficient stock");
            }

            // Deduct stock
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

        // 5️⃣ Create order
        Order order = Order.builder()
                .customer(customer)
                .shippingAddress(shippingAddress)
                .totalPrice(totalPrice)
                .status(Order.Status.PENDING)
                .paymentStatus(
                        request.getPaymentMethod().equals("COD") ?
                                Order.PaymentStatus.COD : Order.PaymentStatus.PAID)
                .orderItems(new ArrayList<>())
                .build();

        // Link order items
        for (OrderItem item : orderItems) {
            item.setOrder(order);
        }
        order.setOrderItems(orderItems);

        Order savedOrder = orderRepository.save(order);

        // 6️⃣ Build response DTO
        List<CartItemDto> responseItems = new ArrayList<>();
        for (OrderItem item : savedOrder.getOrderItems()) {
            CartItemDto dto = new CartItemDto();
            dto.setId(item.getId()); 
            dto.setBookId(item.getBook().getId());
            dto.setTitle(item.getBook().getTitle());
            dto.setAuthor(item.getBook().getAuthor());
            dto.setQuantity(item.getQuantity());
            dto.setPrice(item.getUnitPrice());
            dto.setTotal(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            responseItems.add(dto);
        }

        AddressResponseDto addressDto = new AddressResponseDto(
                shippingAddress.getId(),
                shippingAddress.getCity(),
                shippingAddress.getState(),
                shippingAddress.getCountry()
        );

        OrderResponseDto response = new OrderResponseDto();
        response.setOrderId(savedOrder.getId());
        response.setCustomerId(savedOrder.getCustomer().getId());
        response.setPaymentMethod(request.getPaymentMethod());
        response.setStatus(savedOrder.getStatus().name());
        response.setTotalAmount(savedOrder.getTotalPrice().doubleValue());
        response.setItems(responseItems);
        response.setShippingAddress(addressDto);

        return response;
    }
    //For Customer
	@Override
	public OrderResponseDto getOrderById(Long orderId) {
		// 1️⃣ Get current authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new UnauthorizedException("User not authenticated");
        }

        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();

        // 2️⃣ Fetch order
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));

        // 3️⃣ Authorization: only owner or admin
        if (!order.getCustomer().getId().equals(userDetails.getId()) && !userDetails.isAdmin()) {
            throw new UnauthorizedException("Access denied");
        }

        // 4️⃣ Map entity to DTO
        OrderResponseDto response = modelMapper.map(order, OrderResponseDto.class);
        return response;
	}
	
	//Get Order by id For Admin
	@Override
    @Transactional
    public AdminOrderResponseDto getOrderByAdminId(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));

        // Map Order → AdminOrderResponseDto
        AdminOrderResponseDto response = modelMapper.map(order, AdminOrderResponseDto.class);

        // Fill custom fields
        response.setOrderId(order.getId());
        response.setBuyerId(order.getCustomer().getId());
        response.setBuyerName(order.getCustomer().getName());
        response.setBuyerEmail(order.getCustomer().getEmail());
        response.setTotalAmount(order.getTotalPrice().doubleValue());

        // Map items manually with ModelMapper
        response.setItems(order.getOrderItems().stream()
                .map(item -> modelMapper.map(item, OrderDTO.class))
                .collect(Collectors.toList()));

        return response;
    }
	
	//Update Order Status for Admin
	@Override
    public OrderDTO updateOrderStatus(Long orderId, String status) {
        // ✅ Fetch order
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));

        try {
            Order.Status newStatus = Order.Status.valueOf(status.toUpperCase());

            // ✅ Validate lifecycle (cannot revert from DELIVERED)
            if (order.getStatus() == Order.Status.DELIVERED && newStatus != Order.Status.DELIVERED) {
                throw new IllegalArgumentException("Cannot change status of a delivered order");
            }

            // ✅ Update status
            order.setStatus(newStatus);
            Order updatedOrder = orderRepository.save(order);

            // ✅ Convert to DTO using ModelMapper
            OrderDTO dto = modelMapper.map(updatedOrder, OrderDTO.class);

            // manual fix for nested mapping
            dto.setBuyerId(updatedOrder.getCustomer().getId());
            if (!updatedOrder.getOrderItems().isEmpty()) {
                dto.setSellerId(updatedOrder.getOrderItems().get(0).getSeller().getId());
                dto.setBookId(updatedOrder.getOrderItems().get(0).getBook().getId());
            }

            return dto;

        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid status. Allowed: PENDING, SHIPPED, DELIVERED, CANCELLED");
        }
    }



}
