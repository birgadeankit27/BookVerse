package com.bookverser.BookVerse.serviceimpl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.bookverser.BookVerse.dto.*;
import com.bookverser.BookVerse.entity.*;
import com.bookverser.BookVerse.exception.*;
import com.bookverser.BookVerse.repository.*;
import com.bookverser.BookVerse.security.CustomUserDetails;
import com.bookverser.BookVerse.service.OrderService;

import jakarta.transaction.Transactional;

@Service
public class OrderServiceImpl implements OrderService {

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

    private static final int RETURN_DAYS_LIMIT = 7;

    // Constructor with simplified ModelMapper configuration
    public OrderServiceImpl(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
        // Map Order to OrderResponseDto (for paymentMethod)
        modelMapper.typeMap(Order.class, OrderResponseDto.class)
                .addMappings(mapper -> mapper.map(src -> src.getPaymentStatus() != null ? src.getPaymentStatus().name() : null, OrderResponseDto::setPaymentMethod));
        // Map OrderItem to CartItemDto (for title, author, total)
        modelMapper.typeMap(OrderItem.class, CartItemDto.class)
                .addMappings(mapper -> {
                    mapper.map(src -> src.getBook() != null ? src.getBook().getId() : null, CartItemDto::setBookId);
                    mapper.map(src -> src.getBook() != null ? src.getBook().getTitle() : null, CartItemDto::setTitle);
                    mapper.map(src -> src.getBook() != null ? src.getBook().getAuthor() : null, CartItemDto::setAuthor);
                    mapper.map(src -> src.getUnitPrice(), CartItemDto::setPrice);
                    mapper.map(src -> src.getUnitPrice() != null ? src.getUnitPrice().multiply(BigDecimal.valueOf(src.getQuantity())) : null, CartItemDto::setTotal);
                });
        // Simplified mapping for OrderItem to OrderDTO
        modelMapper.typeMap(OrderItem.class, OrderDTO.class)
                .addMappings(mapper -> {
                    mapper.map(src -> src.getId(), OrderDTO::setId);
                    mapper.map(src -> src.getBook() != null ? src.getBook().getId() : null, OrderDTO::setBookId);
                    // Avoid complex mappings that might cause null issues
                });
    }

    @Transactional
    @Override
    public OrderResponseDto placeOrder(PlaceOrderRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated())
            throw new UnauthorizedException("User not authenticated");

        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        User customer = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new UnauthorizedException("Customer not found"));

        // Save shipping address
        Address shippingAddress = Address.builder()
                .city(request.getShippingAddress().getCity())
                .state(request.getShippingAddress().getState())
                .country(request.getShippingAddress().getCountry())
                .user(customer)
                .build();
        shippingAddress = addressRepository.save(shippingAddress);

        // Validate payment method
        String method = request.getPaymentMethod().toUpperCase();
        if (!List.of("COD", "CARD", "UPI", "NET_BANKING").contains(method))
            throw new IllegalArgumentException("Invalid payment method: " + method);

        // Map payment method to DB-safe enum
        Order.PaymentStatus paymentStatus = method.equals("COD") ? Order.PaymentStatus.COD : Order.PaymentStatus.PAID;

        // Validate items
        BigDecimal totalPrice = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();
        for (PlaceOrderRequest.OrderItemRequest itemReq : request.getItems()) {
            Book book = bookRepository.findById(itemReq.getBookId())
                    .orElseThrow(() -> new BookNotFoundException("Book not found: " + itemReq.getBookId()));

            if (book.getStock() < itemReq.getQuantity())
                throw new InsufficientStockException("Book " + book.getTitle() + " has insufficient stock");

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

        // Create order
        Order order = Order.builder()
                .customer(customer)
                .shippingAddress(shippingAddress)
                .totalPrice(totalPrice)
                .status(Order.Status.PENDING)
                .paymentStatus(paymentStatus)
                .createdAt(LocalDateTime.now())
                .orderItems(new ArrayList<>())
                .build();

        // Link items
        for (OrderItem item : orderItems) item.setOrder(order);
        order.setOrderItems(orderItems);

        Order savedOrder = orderRepository.save(order);

        // Build response items manually to ensure no nulls
        List<CartItemDto> responseItems = orderItems.stream().map(item -> {
            CartItemDto dto = new CartItemDto();
            dto.setId(item.getId());
            dto.setBookId(item.getBook() != null ? item.getBook().getId() : null);
            dto.setTitle(item.getBook() != null ? item.getBook().getTitle() : null);
            dto.setAuthor(item.getBook() != null ? item.getBook().getAuthor() : null);
            dto.setQuantity(item.getQuantity());
            dto.setPrice(item.getUnitPrice());
            dto.setTotal(item.getUnitPrice() != null ? item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())) : null);
            return dto;
        }).toList();

        AddressResponseDto addressDto = new AddressResponseDto(
                shippingAddress.getId(),
                shippingAddress.getCity(),
                shippingAddress.getState(),
                shippingAddress.getCountry()
        );

        OrderResponseDto response = new OrderResponseDto();
        response.setOrderId(savedOrder.getId());
        response.setCustomerId(customer.getId());
        response.setPaymentMethod(method);
        response.setStatus(savedOrder.getStatus() != null ? savedOrder.getStatus().name() : null);
        response.setTotalAmount(savedOrder.getTotalPrice() != null ? savedOrder.getTotalPrice().doubleValue() : 0.0);
        response.setItems(responseItems);
        response.setShippingAddress(addressDto);

        return response;
    }

    @Override
    public OrderResponseDto getOrderById(Long orderId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) throw new UnauthorizedException("User not authenticated");

        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));

        if (!order.getCustomer().getId().equals(userDetails.getId()) && !userDetails.isAdmin())
            throw new UnauthorizedException("Access denied");

        // Manually build response to ensure no nulls
        OrderResponseDto response = new OrderResponseDto();
        response.setOrderId(order.getId());
        response.setCustomerId(order.getCustomer() != null ? order.getCustomer().getId() : null);
        response.setPaymentMethod(order.getPaymentStatus() != null ? order.getPaymentStatus().name() : null);
        response.setStatus(order.getStatus() != null ? order.getStatus().name() : null);
        response.setTotalAmount(order.getTotalPrice() != null ? order.getTotalPrice().doubleValue() : 0.0);

        List<CartItemDto> responseItems = order.getOrderItems().stream().map(item -> {
            CartItemDto dto = new CartItemDto();
            dto.setId(item.getId());
            dto.setBookId(item.getBook() != null ? item.getBook().getId() : null);
            dto.setTitle(item.getBook() != null ? item.getBook().getTitle() : null);
            dto.setAuthor(item.getBook() != null ? item.getBook().getAuthor() : null);
            dto.setQuantity(item.getQuantity());
            dto.setPrice(item.getUnitPrice());
            dto.setTotal(item.getUnitPrice() != null ? item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())) : null);
            return dto;
        }).toList();
        response.setItems(responseItems);

        AddressResponseDto addressDto = new AddressResponseDto(
                order.getShippingAddress() != null ? order.getShippingAddress().getId() : null,
                order.getShippingAddress() != null ? order.getShippingAddress().getCity() : null,
                order.getShippingAddress() != null ? order.getShippingAddress().getState() : null,
                order.getShippingAddress() != null ? order.getShippingAddress().getCountry() : null
        );
        response.setShippingAddress(addressDto);

        return response;
    }

    @Transactional
    @Override
    public AdminOrderResponseDto getOrderByAdminId(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));

        AdminOrderResponseDto response = new AdminOrderResponseDto();
        response.setOrderId(order.getId());
        response.setBuyerId(order.getCustomer() != null ? order.getCustomer().getId() : null);
        response.setBuyerName(order.getCustomer() != null ? order.getCustomer().getName() : null);
        response.setBuyerEmail(order.getCustomer() != null ? order.getCustomer().getEmail() : null);
        response.setStatus(order.getStatus() != null ? order.getStatus().name() : null);
        response.setPaymentStatus(order.getPaymentStatus() != null ? order.getPaymentStatus().name() : null);
        response.setTotalAmount(order.getTotalPrice() != null ? order.getTotalPrice().doubleValue() : 0.0);

        // Manually map OrderDTO to avoid ModelMapper issues
        List<OrderDTO> items = order.getOrderItems().stream().map(item -> {
            OrderDTO dto = new OrderDTO();
            dto.setId(item.getId());
            dto.setBuyerId(order.getCustomer() != null ? order.getCustomer().getId() : null);
            dto.setSellerId(item.getSeller() != null ? item.getSeller().getId() : null);
            dto.setBookId(item.getBook() != null ? item.getBook().getId() : null);
            dto.setStatus(order.getStatus() != null ? order.getStatus().name() : null);
            dto.setPaymentStatus(order.getPaymentStatus() != null ? order.getPaymentStatus().name() : null);
            return dto;
        }).collect(Collectors.toList());
        response.setItems(items);

        AddressResponseDto addressDto = new AddressResponseDto(
                order.getShippingAddress() != null ? order.getShippingAddress().getId() : null,
                order.getShippingAddress() != null ? order.getShippingAddress().getCity() : null,
                order.getShippingAddress() != null ? order.getShippingAddress().getState() : null,
                order.getShippingAddress() != null ? order.getShippingAddress().getCountry() : null
        );
        response.setShippingAddress(addressDto);

        return response;
    }

    @Transactional
    @Override
    public OrderResponseDto requestReturn(Long orderId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) throw new UnauthorizedException("User not authenticated");

        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));

        if (!order.getCustomer().getId().equals(userDetails.getId()) && !userDetails.isAdmin())
            throw new UnauthorizedException("Access denied");

        if (order.getStatus() != Order.Status.DELIVERED)
            throw new InvalidReturnRequestException("Order is not eligible for return.");

        if (order.getCreatedAt().plusDays(RETURN_DAYS_LIMIT).isBefore(LocalDateTime.now()))
            throw new InvalidReturnRequestException("Return period expired.");

        order.setStatus(Order.Status.RETURN_REQUESTED);
        order.setPaymentStatus(Order.PaymentStatus.PAID);
        Order updatedOrder = orderRepository.save(order);

        // Manually build response to ensure no nulls
        OrderResponseDto response = new OrderResponseDto();
        response.setOrderId(updatedOrder.getId());
        response.setCustomerId(updatedOrder.getCustomer() != null ? updatedOrder.getCustomer().getId() : null);
        response.setPaymentMethod(updatedOrder.getPaymentStatus() != null ? updatedOrder.getPaymentStatus().name() : null);
        response.setStatus(updatedOrder.getStatus() != null ? updatedOrder.getStatus().name() : null);
        response.setTotalAmount(updatedOrder.getTotalPrice() != null ? updatedOrder.getTotalPrice().doubleValue() : 0.0);

        List<CartItemDto> responseItems = updatedOrder.getOrderItems().stream().map(item -> {
            CartItemDto dto = new CartItemDto();
            dto.setId(item.getId());
            dto.setBookId(item.getBook() != null ? item.getBook().getId() : null);
            dto.setTitle(item.getBook() != null ? item.getBook().getTitle() : null);
            dto.setAuthor(item.getBook() != null ? item.getBook().getAuthor() : null);
            dto.setQuantity(item.getQuantity());
            dto.setPrice(item.getUnitPrice());
            dto.setTotal(item.getUnitPrice() != null ? item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())) : null);
            return dto;
        }).toList();
        response.setItems(responseItems);

        AddressResponseDto addressDto = new AddressResponseDto(
                updatedOrder.getShippingAddress() != null ? updatedOrder.getShippingAddress().getId() : null,
                updatedOrder.getShippingAddress() != null ? updatedOrder.getShippingAddress().getCity() : null,
                updatedOrder.getShippingAddress() != null ? updatedOrder.getShippingAddress().getState() : null,
                updatedOrder.getShippingAddress() != null ? updatedOrder.getShippingAddress().getCountry() : null
        );
        response.setShippingAddress(addressDto);

        return response;
    }
}