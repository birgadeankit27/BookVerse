package com.bookverser.BookVerse.serviceimpl;

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
import com.bookverser.BookVerse.exception.OrderNotFoundException;
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

	@Override
	public List<OrderResponseDto> getMyOrders(Long userId) {
	    // 1️⃣ User अस्तित्वात आहे का ते तपासा
	    User customer = userRepository.findById(userId)
	            .orElseThrow(() -> new UnauthorizedException("Customer not found"));

	    // 2️⃣ User चे orders fetch करा
	    List<Order> orders = orderRepository.findByCustomerId(customer.getId());
	    if (orders.isEmpty()) {
	        throw new OrderNotFoundException("No orders found for this user");
	    }

	    // 3️⃣ Entity → DTO convert करा
	    return orders.stream()
	            .map(order -> {
	                OrderResponseDto dto = new OrderResponseDto();
	                dto.setOrderId(order.getId());
	                dto.setCustomerId(order.getCustomer().getId());
	                dto.setPaymentMethod(order.getPaymentStatus().name());
	                dto.setStatus(order.getStatus().name());
	                dto.setTotalAmount(order.getTotalPrice().doubleValue());

	                // Items
	                List<CartItemDto> itemDtos = order.getOrderItems().stream().map(item -> {
	                    CartItemDto cartItemDto = new CartItemDto();
	                    cartItemDto.setId(item.getId());
	                    cartItemDto.setBookId(item.getBook().getId());
	                    cartItemDto.setTitle(item.getBook().getTitle());
	                    cartItemDto.setAuthor(item.getBook().getAuthor());
	                    cartItemDto.setQuantity(item.getQuantity());
	                    cartItemDto.setPrice(item.getUnitPrice());
	                    cartItemDto.setTotal(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
	                    return cartItemDto;
	                }).collect(Collectors.toList());

	                dto.setItems(itemDtos);

	                // Address
	                Address address = order.getShippingAddress();
	                if (address != null) {
	                    dto.setShippingAddress(new AddressResponseDto(
	                            address.getId(),
	                            address.getCity(),
	                            address.getState(),
	                            address.getCountry()
	                    ));
	                }

	                return dto;
	            })
	            .collect(Collectors.toList());
	}

	 @Transactional
	    @Override
	    public void deleteOrder(Long orderId) {
	        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();

	        Order order = orderRepository.findById(orderId)
	                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));

	        // Security check: Only admin can delete
	        if (!userDetails.isAdmin()) {
	            throw new UnauthorizedException("Only admins can delete orders");
	        }

	        orderRepository.delete(order);
	    }

	
	
}
