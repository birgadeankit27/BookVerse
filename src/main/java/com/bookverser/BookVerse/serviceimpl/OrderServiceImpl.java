package com.bookverser.BookVerse.serviceimpl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.bookverser.BookVerse.dto.CartItemDto;
import com.bookverser.BookVerse.dto.OrderResponseDto;
import com.bookverser.BookVerse.dto.PlaceOrderRequest;
import com.bookverser.BookVerse.dto.AddressResponseDto;
import com.bookverser.BookVerse.entity.Address;
import com.bookverser.BookVerse.entity.Book;
import com.bookverser.BookVerse.entity.Order;
import com.bookverser.BookVerse.entity.OrderItem;
import com.bookverser.BookVerse.entity.User;
import com.bookverser.BookVerse.exception.BookNotFoundException;
import com.bookverser.BookVerse.exception.InsufficientStockException;
import com.bookverser.BookVerse.exception.UnauthorizedException;
import com.bookverser.BookVerse.repository.AddressRepository;
import com.bookverser.BookVerse.repository.BookRepository;
import com.bookverser.BookVerse.repository.OrderRepository;
import com.bookverser.BookVerse.repository.UserRepository;
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
}
