package com.bookverser.BookVerse.serviceimpl;


import java.io.ByteArrayOutputStream;

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


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import com.bookverser.BookVerse.dto.*;
import com.bookverser.BookVerse.entity.*;
import com.bookverser.BookVerse.entity.Order.PaymentStatus;
import com.bookverser.BookVerse.exception.*;
import com.bookverser.BookVerse.repository.*;


import com.bookverser.BookVerse.dto.*;
import com.bookverser.BookVerse.entity.*;
import com.bookverser.BookVerse.exception.*;
import com.bookverser.BookVerse.repository.*;
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
import com.bookverser.BookVerse.exception.InvalidOrderStatusException;
import com.bookverser.BookVerse.exception.OrderNotFoundException;
import com.bookverser.BookVerse.exception.UnauthorizedException;
import com.bookverser.BookVerse.repository.AddressRepository;
import com.bookverser.BookVerse.repository.BookRepository;
import com.bookverser.BookVerse.repository.OrderRepository;
import com.bookverser.BookVerse.repository.UserRepository;

import com.bookverser.BookVerse.security.CustomUserDetails;

import com.bookverser.BookVerse.service.OrderService;

import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

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
				.addMappings(mapper -> mapper.map(
						src -> src.getPaymentStatus() != null ? src.getPaymentStatus().name() : null,
						OrderResponseDto::setPaymentMethod));
		// Map OrderItem to CartItemDto (for title, author, total)
		modelMapper.typeMap(OrderItem.class, CartItemDto.class).addMappings(mapper -> {
			mapper.map(src -> src.getBook() != null ? src.getBook().getId() : null, CartItemDto::setBookId);
			mapper.map(src -> src.getBook() != null ? src.getBook().getTitle() : null, CartItemDto::setTitle);
			mapper.map(src -> src.getBook() != null ? src.getBook().getAuthor() : null, CartItemDto::setAuthor);
			mapper.map(src -> src.getUnitPrice(), CartItemDto::setPrice);
			mapper.map(src -> src.getUnitPrice() != null
					? src.getUnitPrice().multiply(BigDecimal.valueOf(src.getQuantity()))
					: null, CartItemDto::setTotal);
		});
		// Simplified mapping for OrderItem to OrderDTO
		modelMapper.typeMap(OrderItem.class, OrderDTO.class).addMappings(mapper -> {
			mapper.map(src -> src.getId(), OrderDTO::setId);
			mapper.map(src -> src.getBook() != null ? src.getBook().getId() : null, OrderDTO::setBookId);
			// Avoid complex mappings that might cause null issues
		});

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
	
	//Update Order Status for Admin
	@Transactional
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
	@Override
    public OrderDTO cancelOrder(Long orderId, Long userId, boolean isAdmin) {
        // 1️⃣ Check if order exists
        Optional<Order> optionalOrder = orderRepository.findById(orderId);
        if (optionalOrder.isEmpty()) {
            throw new OrderNotFoundException("Order not found with id: " + orderId);
        }

        Order order = optionalOrder.get();

        // 2️⃣ Check authorization (only customer or admin can cancel)
        if (!isAdmin && (order.getCustomer() == null || !order.getCustomer().getId().equals(userId))) {
            throw new UnauthorizedException("You are not authorized to cancel this order.");
        }



        // 3️⃣ Validate order status
        if (!(order.getStatus() == Order.Status.PENDING ||
        	      order.getStatus() == Order.Status.CONFIRMED)) {
        	    throw new InvalidOrderStatusException(
        	        "Order cannot be cancelled as it is already " + order.getStatus());
        	}


        // 4️⃣ Update order status to CANCELLED
        order.setStatus(Order.Status.CANCELLED);
        Order updatedOrder = orderRepository.save(order);

        // 5️⃣ Convert Entity → DTO using ModelMapper
        return modelMapper.map(updatedOrder, OrderDTO.class);
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
}

	@Override
	@Transactional(readOnly = true)
	public byte[] generateInvoicePdf(Long orderId) {

		// --- Verify admin access ---
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
			throw new UnauthorizedException("Only admins can generate invoices.");
		}

		// --- Fetch order ---
		Order order = orderRepository.findById(orderId)
				.orElseThrow(() -> new OrderNotFoundException("Order not found with ID: " + orderId));

		// --- Extract only simple data to avoid recursion ---
		String customerName = order.getCustomer() != null ? order.getCustomer().getName() : "N/A";
		String customerEmail = order.getCustomer() != null ? order.getCustomer().getEmail() : "N/A";
		String shippingAddress = order.getShippingAddress() != null
				? (order.getShippingAddress().getCity() + order.getShippingAddress().getCountry()
						+ order.getShippingAddress().getCountry())
				: "N/A";
		LocalDateTime orderDate = order.getCreatedAt();
		BigDecimal totalPrice = order.getTotalPrice();
		PaymentStatus paymentStatus = order.getPaymentStatus();

		try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
			Document document = new Document(PageSize.A4);
			PdfWriter.getInstance(document, outputStream);
			document.open();

			// --- Title ---
			Font titleFont = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD);
			Paragraph title = new Paragraph("BookVerse Invoice", titleFont);
			title.setAlignment(Element.ALIGN_CENTER);
			document.add(title);
			document.add(Chunk.NEWLINE);

			// --- Order info ---
			Font infoFont = new Font(Font.FontFamily.HELVETICA, 12);
			document.add(new Paragraph("Order ID: " + orderId, infoFont));
			document.add(new Paragraph("Order Date: " + orderDate, infoFont));
			document.add(new Paragraph("Customer: " + customerName, infoFont));
			document.add(new Paragraph("Email: " + customerEmail, infoFont));
			document.add(new Paragraph("Shipping Address: " + shippingAddress, infoFont));
			document.add(Chunk.NEWLINE);

			// --- Items table ---
			PdfPTable table = new PdfPTable(4);
			table.setWidthPercentage(100);
			table.setWidths(new int[] { 4, 1, 2, 2 });
			Font headFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
			table.addCell(new PdfPCell(new Phrase("Book Title", headFont)));
			table.addCell(new PdfPCell(new Phrase("Qty", headFont)));
			table.addCell(new PdfPCell(new Phrase("Unit Price", headFont)));
			table.addCell(new PdfPCell(new Phrase("Subtotal", headFont)));

			List<OrderItem> items = order.getOrderItems() != null
			        ? new ArrayList<>(order.getOrderItems())
			        : new ArrayList<>();
			for (OrderItem item : items) {
				String titleTxt = item.getBook() != null ? item.getBook().getTitle() : "N/A";
				BigDecimal price = item.getBook() != null ? item.getBook().getPrice() : BigDecimal.ZERO;
				int qty = item.getQuantity();
				BigDecimal subTot = price.multiply(BigDecimal.valueOf(qty));

				table.addCell(titleTxt);
				table.addCell(String.valueOf(qty));
				table.addCell("₹" + price);
				table.addCell("₹" + subTot);
			}
			document.add(table);
			document.add(Chunk.NEWLINE);

			// --- Totals & footer ---
			Font totalFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
			Paragraph total = new Paragraph("Total: ₹" + totalPrice, totalFont);
			total.setAlignment(Element.ALIGN_RIGHT);
			document.add(total);

			document.add(new Paragraph("Payment Status: " + paymentStatus, infoFont));
			document.add(Chunk.NEWLINE);

			Paragraph footer = new Paragraph("Thank you for shopping with BookVerse!", infoFont);
			footer.setAlignment(Element.ALIGN_CENTER);
			document.add(footer);

			document.close();
			return outputStream.toByteArray();

		} catch (Exception e) {
			throw new RuntimeException("Error generating invoice PDF: " + e.getMessage(), e);
		}
	}
