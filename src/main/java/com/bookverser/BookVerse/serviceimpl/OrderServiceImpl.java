


package com.bookverser.BookVerse.serviceimpl;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bookverser.BookVerse.dto.AddressResponseDto;
import com.bookverser.BookVerse.dto.AdminOrderResponseDto;
import com.bookverser.BookVerse.dto.BulkOrderStatusUpdateRequest;
import com.bookverser.BookVerse.dto.CartItemDto;
import com.bookverser.BookVerse.dto.OrderDTO;
import com.bookverser.BookVerse.dto.OrderResponseDto;
import com.bookverser.BookVerse.dto.OrderSummaryDto;
import com.bookverser.BookVerse.dto.PlaceOrderRequest;
import com.bookverser.BookVerse.entity.Address;
import com.bookverser.BookVerse.entity.Book;
import com.bookverser.BookVerse.entity.Order;
import com.bookverser.BookVerse.entity.OrderItem;
import com.bookverser.BookVerse.entity.Order.PaymentStatus;
import com.bookverser.BookVerse.entity.User;
import com.bookverser.BookVerse.exception.BookNotFoundException;
import com.bookverser.BookVerse.exception.InsufficientStockException;
import com.bookverser.BookVerse.exception.InvalidOrderStatusException;
import com.bookverser.BookVerse.exception.InvalidReturnRequestException;
import com.bookverser.BookVerse.exception.OrderNotFoundException;
import com.bookverser.BookVerse.exception.UnauthorizedException;
import com.bookverser.BookVerse.repository.AddressRepository;
import com.bookverser.BookVerse.repository.BookRepository;
import com.bookverser.BookVerse.repository.OrderRepository;
import com.bookverser.BookVerse.repository.UserRepository;
import com.bookverser.BookVerse.security.CustomUserDetails;
import com.bookverser.BookVerse.service.OrderService;

import org.modelmapper.ModelMapper;

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

@Service
public class OrderServiceImpl implements OrderService {

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final OrderRepository orderRepository;
    private final AddressRepository addressRepository;
    private final ModelMapper modelMapper;

    private static final int RETURN_DAYS_LIMIT = 7;

    // Constructor
    public OrderServiceImpl(
            UserRepository userRepository,
            BookRepository bookRepository,
            OrderRepository orderRepository,
            AddressRepository addressRepository,
            ModelMapper modelMapper) {

        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.orderRepository = orderRepository;
        this.addressRepository = addressRepository;
        this.modelMapper = modelMapper;

        // Order -> OrderResponseDto
        modelMapper.typeMap(Order.class, OrderResponseDto.class)
                .addMappings(mapper -> mapper.map(
                        src -> src.getPaymentStatus() != null
                                ? src.getPaymentStatus().name()
                                : null,
                        OrderResponseDto::setPaymentMethod));

        // OrderItem -> CartItemDto
        modelMapper.typeMap(OrderItem.class, CartItemDto.class)
                .addMappings(mapper -> {

                    mapper.map(
                            src -> src.getBook() != null
                                    ? src.getBook().getId()
                                    : null,
                            CartItemDto::setBookId);

                    mapper.map(
                            src -> src.getBook() != null
                                    ? src.getBook().getTitle()
                                    : null,
                            CartItemDto::setTitle);

                    mapper.map(
                            src -> src.getBook() != null
                                    ? src.getBook().getAuthor()
                                    : null,
                            CartItemDto::setAuthor);

                    mapper.map(
                            src -> src.getUnitPrice(),
                            CartItemDto::setPrice);

                    mapper.map(
                            src -> src.getUnitPrice() != null
                                    ? src.getUnitPrice()
                                            .multiply(BigDecimal.valueOf(src.getQuantity()))
                                    : null,
                            CartItemDto::setTotal);
                });

        // OrderItem -> OrderDTO
        modelMapper.typeMap(OrderItem.class, OrderDTO.class)
                .addMappings(mapper -> {

                    mapper.map(
                            src -> src.getId(),
                            OrderDTO::setId);

                    mapper.map(
                            src -> src.getBook() != null
                                    ? src.getBook().getId()
                                    : null,
                            OrderDTO::setBookId);
                });
    }

    // =========================================================
    // CUSTOMER: GET MY ORDERS
    // =========================================================

    @Override
    public List<OrderResponseDto> getMyOrders(String email) {

        User customer = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UnauthorizedException("Customer not found"));

        List<Order> orders = orderRepository.findAll()
                .stream()
                .filter(order ->
                        order.getCustomer() != null
                                && order.getCustomer().getId().equals(customer.getId()))
                .collect(Collectors.toList());

        return orders.stream()
                .map(order -> {

                    OrderResponseDto dto = new OrderResponseDto();

                    dto.setOrderId(order.getId());

                    dto.setCustomerId(
                            order.getCustomer() != null
                                    ? order.getCustomer().getId()
                                    : null);

                    dto.setPaymentMethod(
                            order.getPaymentStatus() != null
                                    ? order.getPaymentStatus().name()
                                    : null);

                    dto.setStatus(
                            order.getStatus() != null
                                    ? order.getStatus().name()
                                    : null);

                    dto.setTotalAmount(
                            order.getTotalPrice() != null
                                    ? order.getTotalPrice().doubleValue()
                                    : 0.0);

                    List<CartItemDto> items = order.getOrderItems()
                            .stream()
                            .map(item -> {

                                CartItemDto cartItemDto = new CartItemDto();

                                cartItemDto.setId(item.getId());

                                cartItemDto.setBookId(
                                        item.getBook() != null
                                                ? item.getBook().getId()
                                                : null);

                                cartItemDto.setTitle(
                                        item.getBook() != null
                                                ? item.getBook().getTitle()
                                                : null);

                                cartItemDto.setAuthor(
                                        item.getBook() != null
                                                ? item.getBook().getAuthor()
                                                : null);

                                cartItemDto.setQuantity(item.getQuantity());

                                cartItemDto.setPrice(item.getUnitPrice());

                                return cartItemDto;
                            })
                            .collect(Collectors.toList());

                    dto.setItems(items);

                    return dto;
                })
                .collect(Collectors.toList());
    }

    // =========================================================
    // ADMIN: GET ALL ORDERS
    // =========================================================

    @Override
    public Page<OrderSummaryDto> getAllOrders(
            String status,
            LocalDate fromDate,
            LocalDate toDate,
            Long customerId,
            Pageable pageable) {

        List<Order> orders = orderRepository.findAll();

        List<OrderSummaryDto> filtered = orders.stream()

                .filter(order ->
                        status == null
                                || (order.getStatus() != null
                                && order.getStatus().name().equalsIgnoreCase(status)))

                .filter(order ->
                        fromDate == null
                                || !order.getCreatedAt()
                                .toLocalDate()
                                .isBefore(fromDate))

                .filter(order ->
                        toDate == null
                                || !order.getCreatedAt()
                                .toLocalDate()
                                .isAfter(toDate))

                .filter(order ->
                        customerId == null
                                || (order.getCustomer() != null
                                && order.getCustomer().getId().equals(customerId)))

                .map(order -> new OrderSummaryDto(
                        order.getId(),
                        order.getCustomer() != null
                                ? order.getCustomer().getId()
                                : null,
                        order.getCustomer() != null
                                ? order.getCustomer().getEmail()
                                : null,
                        order.getStatus() != null
                                ? order.getStatus().name()
                                : null,
                        order.getPaymentStatus() != null
                                ? order.getPaymentStatus().name()
                                : null,
                        order.getTotalPrice(),
                        order.getCreatedAt()
                ))
                .collect(Collectors.toList());

        int start = (int) pageable.getOffset();

        int end = Math.min(
                start + pageable.getPageSize(),
                filtered.size());

        if (start > filtered.size()) {
            start = filtered.size();
        }

        List<OrderSummaryDto> pagedList =
                filtered.subList(start, end);

        return new PageImpl<>(
                pagedList,
                pageable,
                filtered.size());
    }

    // =========================================================
    // ADMIN: BULK UPDATE ORDER STATUS
    // =========================================================

    @Override
    @Transactional
    public List<OrderResponseDto> bulkUpdateOrderStatus(
            BulkOrderStatusUpdateRequest request) {

        List<Long> orderIds = request.getOrderIds();

        if (orderIds == null || orderIds.isEmpty()) {
            throw new IllegalArgumentException(
                    "Order IDs cannot be empty");
        }

        String newStatus = request.getStatus().toUpperCase();

        Order.Status targetStatus;

        try {
            targetStatus = Order.Status.valueOf(newStatus);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid order status: " + newStatus);
        }

        List<Order> orders =
                orderRepository.findAllById(orderIds);

        if (orders.size() != orderIds.size()) {
            throw new RuntimeException(
                    "Some orders not found. Check the provided IDs.");
        }

        for (Order order : orders) {

            if (order.getStatus() == Order.Status.CANCELLED) {
                throw new RuntimeException(
                        "Cannot update cancelled orders: Order ID "
                                + order.getId());
            }

            if (order.getStatus() == Order.Status.DELIVERED
                    && targetStatus != Order.Status.DELIVERED) {

                throw new RuntimeException(
                        "Delivered order cannot be changed: Order ID "
                                + order.getId());
            }
        }

        orders.forEach(order ->
                order.setStatus(targetStatus));

        orderRepository.saveAll(orders);

        return orders.stream()
                .map(order -> {

                    OrderResponseDto dto =
                            new OrderResponseDto();

                    dto.setOrderId(order.getId());

                    dto.setCustomerId(
                            order.getCustomer() != null
                                    ? order.getCustomer().getId()
                                    : null);

                    dto.setPaymentMethod(
                            order.getPaymentStatus() != null
                                    ? order.getPaymentStatus().name()
                                    : null);

                    dto.setStatus(
                            order.getStatus() != null
                                    ? order.getStatus().name()
                                    : null);

                    dto.setTotalAmount(
                            order.getTotalPrice() != null
                                    ? order.getTotalPrice().doubleValue()
                                    : 0.0);

                    List<CartItemDto> items =
                            order.getOrderItems()
                                    .stream()
                                    .map(item -> {

                                        CartItemDto cartItemDto =
                                                new CartItemDto();

                                        cartItemDto.setId(item.getId());

                                        cartItemDto.setBookId(
                                                item.getBook() != null
                                                        ? item.getBook().getId()
                                                        : null);

                                        cartItemDto.setQuantity(
                                                item.getQuantity());

                                        cartItemDto.setPrice(
                                                item.getUnitPrice());

                                        return cartItemDto;
                                    })
                                    .collect(Collectors.toList());

                    dto.setItems(items);

                    return dto;
                })
                .collect(Collectors.toList());
    }

    // =========================================================
    // CUSTOMER: PLACE ORDER
    // =========================================================

    @Override
    @Transactional
    public OrderResponseDto placeOrder(
            PlaceOrderRequest request) {

        Authentication auth =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new UnauthorizedException(
                    "User not authenticated");
        }

        CustomUserDetails userDetails =
                (CustomUserDetails) auth.getPrincipal();

        User customer =
                userRepository.findById(userDetails.getId())
                        .orElseThrow(() ->
                                new UnauthorizedException(
                                        "Customer not found"));

        Address shippingAddress = Address.builder()
                .city(request.getShippingAddress().getCity())
                .state(request.getShippingAddress().getState())
                .country(request.getShippingAddress().getCountry())
                .user(customer)
                .build();

        shippingAddress =
                addressRepository.save(shippingAddress);

        String method =
                request.getPaymentMethod().toUpperCase();

        if (!List.of(
                "COD",
                "CARD",
                "UPI",
                "NET_BANKING"
        ).contains(method)) {

            throw new IllegalArgumentException(
                    "Invalid payment method: " + method);
        }

        Order.PaymentStatus paymentStatus =
                method.equals("COD")
                        ? Order.PaymentStatus.COD
                        : Order.PaymentStatus.PAID;

        BigDecimal totalPrice = BigDecimal.ZERO;

        List<OrderItem> orderItems =
                new ArrayList<>();

        for (PlaceOrderRequest.OrderItemRequest itemReq
                : request.getItems()) {

            Book book =
                    bookRepository.findById(itemReq.getBookId())
                            .orElseThrow(() ->
                                    new BookNotFoundException(
                                            "Book not found: "
                                                    + itemReq.getBookId()));

            if (book.getStock() < itemReq.getQuantity()) {

                throw new InsufficientStockException(
                        "Book " + book.getTitle()
                                + " has insufficient stock");
            }

            book.setStock(
                    book.getStock()
                            - itemReq.getQuantity());

            bookRepository.save(book);

            OrderItem orderItem =
                    OrderItem.builder()
                            .book(book)
                            .seller(book.getSeller())
                            .quantity(itemReq.getQuantity())
                            .unitPrice(book.getPrice())
                            .build();

            totalPrice =
                    totalPrice.add(
                            book.getPrice()
                                    .multiply(
                                            BigDecimal.valueOf(
                                                    itemReq.getQuantity())));

            orderItems.add(orderItem);
        }

        Order order =
                Order.builder()
                        .customer(customer)
                        .shippingAddress(shippingAddress)
                        .totalPrice(totalPrice)
                        .status(Order.Status.PENDING)
                        .paymentStatus(paymentStatus)
                        .createdAt(LocalDateTime.now())
                        .orderItems(new ArrayList<>())
                        .build();

        for (OrderItem item : orderItems) {
            item.setOrder(order);
        }

        order.setOrderItems(orderItems);

        Order savedOrder =
                orderRepository.save(order);

        List<CartItemDto> responseItems =
                orderItems.stream()
                        .map(item -> {

                            CartItemDto dto =
                                    new CartItemDto();

                            dto.setId(item.getId());

                            dto.setBookId(
                                    item.getBook() != null
                                            ? item.getBook().getId()
                                            : null);

                            dto.setTitle(
                                    item.getBook() != null
                                            ? item.getBook().getTitle()
                                            : null);

                            dto.setAuthor(
                                    item.getBook() != null
                                            ? item.getBook().getAuthor()
                                            : null);

                            dto.setQuantity(
                                    item.getQuantity());

                            dto.setPrice(
                                    item.getUnitPrice());

                            dto.setTotal(
                                    item.getUnitPrice() != null
                                            ? item.getUnitPrice()
                                            .multiply(
                                                    BigDecimal.valueOf(
                                                            item.getQuantity()))
                                            : null);

                            return dto;
                        })
                        .collect(Collectors.toList());

        AddressResponseDto addressDto =
                new AddressResponseDto(
                        shippingAddress.getId(),
                        shippingAddress.getCity(),
                        shippingAddress.getState(),
                        shippingAddress.getCountry());

        OrderResponseDto response =
                new OrderResponseDto();

        response.setOrderId(
                savedOrder.getId());

        response.setCustomerId(
                customer.getId());

        response.setPaymentMethod(method);

        response.setStatus(
                savedOrder.getStatus() != null
                        ? savedOrder.getStatus().name()
                        : null);

        response.setTotalAmount(
                savedOrder.getTotalPrice() != null
                        ? savedOrder.getTotalPrice().doubleValue()
                        : 0.0);

        response.setItems(responseItems);

        response.setShippingAddress(addressDto);

        return response;
    }

    // =========================================================
    // CUSTOMER: GET ORDER BY ID
    // =========================================================

    @Override
    public OrderResponseDto getOrderById(Long orderId) {

        Authentication auth =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new UnauthorizedException(
                    "User not authenticated");
        }

        CustomUserDetails userDetails =
                (CustomUserDetails) auth.getPrincipal();

        Order order =
                orderRepository.findById(orderId)
                        .orElseThrow(() ->
                                new OrderNotFoundException(
                                        "Order not found with id: "
                                                + orderId));

        if (!order.getCustomer().getId()
                .equals(userDetails.getId())
                && !userDetails.isAdmin()) {

            throw new UnauthorizedException(
                    "Access denied");
        }

        return buildOrderResponse(order);
    }

    // =========================================================
    // ADMIN: GET ORDER BY ID
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public AdminOrderResponseDto getOrderByAdminId(
            Long orderId) {

        Order order =
                orderRepository.findById(orderId)
                        .orElseThrow(() ->
                                new OrderNotFoundException(
                                        "Order not found with id: "
                                                + orderId));

        AdminOrderResponseDto response =
                new AdminOrderResponseDto();

        response.setOrderId(order.getId());

        response.setBuyerId(
                order.getCustomer() != null
                        ? order.getCustomer().getId()
                        : null);

        response.setBuyerName(
                order.getCustomer() != null
                        ? order.getCustomer().getName()
                        : null);

        response.setBuyerEmail(
                order.getCustomer() != null
                        ? order.getCustomer().getEmail()
                        : null);

        response.setStatus(
                order.getStatus() != null
                        ? order.getStatus().name()
                        : null);

        response.setPaymentStatus(
                order.getPaymentStatus() != null
                        ? order.getPaymentStatus().name()
                        : null);

        response.setTotalAmount(
                order.getTotalPrice() != null
                        ? order.getTotalPrice().doubleValue()
                        : 0.0);

        List<OrderDTO> items =
                order.getOrderItems()
                        .stream()
                        .map(item -> {

                            OrderDTO dto =
                                    new OrderDTO();

                            dto.setId(item.getId());

                            dto.setBuyerId(
                                    order.getCustomer() != null
                                            ? order.getCustomer().getId()
                                            : null);

                            dto.setSellerId(
                                    item.getSeller() != null
                                            ? item.getSeller().getId()
                                            : null);

                            dto.setBookId(
                                    item.getBook() != null
                                            ? item.getBook().getId()
                                            : null);

                            dto.setStatus(
                                    order.getStatus() != null
                                            ? order.getStatus().name()
                                            : null);

                            dto.setPaymentStatus(
                                    order.getPaymentStatus() != null
                                            ? order.getPaymentStatus().name()
                                            : null);

                            return dto;
                        })
                        .collect(Collectors.toList());

        response.setItems(items);

        AddressResponseDto addressDto =
                new AddressResponseDto(
                        order.getShippingAddress() != null
                                ? order.getShippingAddress().getId()
                                : null,

                        order.getShippingAddress() != null
                                ? order.getShippingAddress().getCity()
                                : null,

                        order.getShippingAddress() != null
                                ? order.getShippingAddress().getState()
                                : null,

                        order.getShippingAddress() != null
                                ? order.getShippingAddress().getCountry()
                                : null);

        response.setShippingAddress(addressDto);

        return response;
    }

    // =========================================================
    // ADMIN: UPDATE ORDER STATUS
    // =========================================================

    @Override
    @Transactional
    public OrderDTO updateOrderStatus(
            Long orderId,
            String status) {

        Order order =
                orderRepository.findById(orderId)
                        .orElseThrow(() ->
                                new OrderNotFoundException(
                                        "Order not found with id: "
                                                + orderId));

        try {

            Order.Status newStatus =
                    Order.Status.valueOf(
                            status.toUpperCase());

            if (order.getStatus() == Order.Status.DELIVERED
                    && newStatus != Order.Status.DELIVERED) {

                throw new IllegalArgumentException(
                        "Cannot change status of a delivered order");
            }

            order.setStatus(newStatus);

            Order updatedOrder =
                    orderRepository.save(order);

            OrderDTO dto =
                    modelMapper.map(
                            updatedOrder,
                            OrderDTO.class);

            if (updatedOrder.getCustomer() != null) {
                dto.setBuyerId(
                        updatedOrder.getCustomer().getId());
            }

            if (updatedOrder.getOrderItems() != null
                    && !updatedOrder.getOrderItems().isEmpty()) {

                OrderItem item =
                        updatedOrder.getOrderItems().get(0);

                if (item.getSeller() != null) {
                    dto.setSellerId(
                            item.getSeller().getId());
                }

                if (item.getBook() != null) {
                    dto.setBookId(
                            item.getBook().getId());
                }
            }

            return dto;

        } catch (IllegalArgumentException ex) {

            throw new IllegalArgumentException(
                    "Invalid status. Allowed: "
                            + "PENDING, CONFIRMED, SHIPPED, "
                            + "DELIVERED, CANCELLED");
        }
    }

    // =========================================================
    // CUSTOMER/ADMIN: CANCEL ORDER
    // =========================================================

    @Override
    @Transactional
    public OrderDTO cancelOrder(
            Long orderId,
            Long userId,
            boolean isAdmin) {

        Optional<Order> optionalOrder =
                orderRepository.findById(orderId);

        if (optionalOrder.isEmpty()) {
            throw new OrderNotFoundException(
                    "Order not found with id: "
                            + orderId);
        }

        Order order = optionalOrder.get();

        if (!isAdmin
                && (order.getCustomer() == null
                || !order.getCustomer()
                .getId()
                .equals(userId))) {

            throw new UnauthorizedException(
                    "You are not authorized to cancel this order.");
        }

        if (!(order.getStatus() == Order.Status.PENDING
                || order.getStatus() == Order.Status.CONFIRMED)) {

            throw new InvalidOrderStatusException(
                    "Order cannot be cancelled as it is already "
                            + order.getStatus());
        }

        order.setStatus(
                Order.Status.CANCELLED);

        Order updatedOrder =
                orderRepository.save(order);

        return modelMapper.map(
                updatedOrder,
                OrderDTO.class);
    }

    // =========================================================
    // CUSTOMER: REQUEST RETURN
    // =========================================================

    @Override
    @Transactional
    public OrderResponseDto requestReturn(
            Long orderId) {

        Authentication auth =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new UnauthorizedException(
                    "User not authenticated");
        }

        CustomUserDetails userDetails =
                (CustomUserDetails) auth.getPrincipal();

        Order order =
                orderRepository.findById(orderId)
                        .orElseThrow(() ->
                                new OrderNotFoundException(
                                        "Order not found with id: "
                                                + orderId));

        if (!order.getCustomer().getId()
                .equals(userDetails.getId())
                && !userDetails.isAdmin()) {

            throw new UnauthorizedException(
                    "Access denied");
        }

        if (order.getStatus()
                != Order.Status.DELIVERED) {

            throw new InvalidReturnRequestException(
                    "Order is not eligible for return.");
        }

        if (order.getCreatedAt()
                .plusDays(RETURN_DAYS_LIMIT)
                .isBefore(LocalDateTime.now())) {

            throw new InvalidReturnRequestException(
                    "Return period expired.");
        }

        order.setStatus(
                Order.Status.RETURN_REQUESTED);

        order.setPaymentStatus(
                Order.PaymentStatus.PAID);

        Order updatedOrder =
                orderRepository.save(order);

        return buildOrderResponse(updatedOrder);
    }

    // =========================================================
    // ADMIN: GENERATE INVOICE PDF
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public byte[] generateInvoicePdf(Long orderId) {

        Authentication auth =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (auth == null
                || !auth.getAuthorities()
                .stream()
                .anyMatch(a ->
                        a.getAuthority()
                                .equals("ROLE_ADMIN"))) {

            throw new UnauthorizedException(
                    "Only admins can generate invoices.");
        }

        Order order =
                orderRepository.findById(orderId)
                        .orElseThrow(() ->
                                new OrderNotFoundException(
                                        "Order not found with ID: "
                                                + orderId));

        String customerName =
                order.getCustomer() != null
                        ? order.getCustomer().getName()
                        : "N/A";

        String customerEmail =
                order.getCustomer() != null
                        ? order.getCustomer().getEmail()
                        : "N/A";

        String shippingAddress =
                order.getShippingAddress() != null
                        ? order.getShippingAddress().getCity()
                        + ", "
                        + order.getShippingAddress().getState()
                        + ", "
                        + order.getShippingAddress().getCountry()
                        : "N/A";

        LocalDateTime orderDate =
                order.getCreatedAt();

        BigDecimal totalPrice =
                order.getTotalPrice();

        PaymentStatus paymentStatus =
                order.getPaymentStatus();

        try (ByteArrayOutputStream outputStream =
                     new ByteArrayOutputStream()) {

            Document document =
                    new Document(PageSize.A4);

            PdfWriter.getInstance(
                    document,
                    outputStream);

            document.open();

            // Title
            Font titleFont =
                    new Font(
                            Font.FontFamily.HELVETICA,
                            20,
                            Font.BOLD);

            Paragraph title =
                    new Paragraph(
                            "BookVerse Invoice",
                            titleFont);

            title.setAlignment(
                    Element.ALIGN_CENTER);

            document.add(title);

            document.add(Chunk.NEWLINE);

            // Order information
            Font infoFont =
                    new Font(
                            Font.FontFamily.HELVETICA,
                            12);

            document.add(
                    new Paragraph(
                            "Order ID: " + orderId,
                            infoFont));

            document.add(
                    new Paragraph(
                            "Order Date: " + orderDate,
                            infoFont));

            document.add(
                    new Paragraph(
                            "Customer: " + customerName,
                            infoFont));

            document.add(
                    new Paragraph(
                            "Email: " + customerEmail,
                            infoFont));

            document.add(
                    new Paragraph(
                            "Shipping Address: "
                                    + shippingAddress,
                            infoFont));

            document.add(Chunk.NEWLINE);

            // Items table
            PdfPTable table =
                    new PdfPTable(4);

            table.setWidthPercentage(100);

            table.setWidths(
                    new int[]{4, 1, 2, 2});

            Font headFont =
                    new Font(
                            Font.FontFamily.HELVETICA,
                            12,
                            Font.BOLD);

            table.addCell(
                    new PdfPCell(
                            new Phrase(
                                    "Book Title",
                                    headFont)));

            table.addCell(
                    new PdfPCell(
                            new Phrase(
                                    "Qty",
                                    headFont)));

            table.addCell(
                    new PdfPCell(
                            new Phrase(
                                    "Unit Price",
                                    headFont)));

            table.addCell(
                    new PdfPCell(
                            new Phrase(
                                    "Subtotal",
                                    headFont)));

            List<OrderItem> items =
                    order.getOrderItems() != null
                            ? new ArrayList<>(
                                    order.getOrderItems())
                            : new ArrayList<>();

            for (OrderItem item : items) {

                String titleTxt =
                        item.getBook() != null
                                ? item.getBook().getTitle()
                                : "N/A";

                BigDecimal price =
                        item.getUnitPrice() != null
                                ? item.getUnitPrice()
                                : BigDecimal.ZERO;

                int qty =
                        item.getQuantity();

                BigDecimal subTotal =
                        price.multiply(
                                BigDecimal.valueOf(qty));

                table.addCell(titleTxt);

                table.addCell(
                        String.valueOf(qty));

                table.addCell(
                        "₹" + price);

                table.addCell(
                        "₹" + subTotal);
            }

            document.add(table);

            document.add(Chunk.NEWLINE);

            // Total
            Font totalFont =
                    new Font(
                            Font.FontFamily.HELVETICA,
                            14,
                            Font.BOLD);

            Paragraph total =
                    new Paragraph(
                            "Total: ₹"
                                    + totalPrice,
                            totalFont);

            total.setAlignment(
                    Element.ALIGN_RIGHT);

            document.add(total);

            document.add(
                    new Paragraph(
                            "Payment Status: "
                                    + paymentStatus,
                            infoFont));

            document.add(Chunk.NEWLINE);

            Paragraph footer =
                    new Paragraph(
                            "Thank you for shopping with BookVerse!",
                            infoFont);

            footer.setAlignment(
                    Element.ALIGN_CENTER);

            document.add(footer);

            document.close();

            return outputStream.toByteArray();

        } catch (Exception e) {

            throw new RuntimeException(
                    "Error generating invoice PDF: "
                            + e.getMessage(),
                    e);
        }
    }

    // =========================================================
    // COMMON METHOD: BUILD ORDER RESPONSE
    // =========================================================

    private OrderResponseDto buildOrderResponse(
            Order order) {

        OrderResponseDto response =
                new OrderResponseDto();

        response.setOrderId(
                order.getId());

        response.setCustomerId(
                order.getCustomer() != null
                        ? order.getCustomer().getId()
                        : null);

        response.setPaymentMethod(
                order.getPaymentStatus() != null
                        ? order.getPaymentStatus().name()
                        : null);

        response.setStatus(
                order.getStatus() != null
                        ? order.getStatus().name()
                        : null);

        response.setTotalAmount(
                order.getTotalPrice() != null
                        ? order.getTotalPrice().doubleValue()
                        : 0.0);

        List<CartItemDto> responseItems =
                order.getOrderItems()
                        .stream()
                        .map(item -> {

                            CartItemDto dto =
                                    new CartItemDto();

                            dto.setId(item.getId());

                            dto.setBookId(
                                    item.getBook() != null
                                            ? item.getBook().getId()
                                            : null);

                            dto.setTitle(
                                    item.getBook() != null
                                            ? item.getBook().getTitle()
                                            : null);

                            dto.setAuthor(
                                    item.getBook() != null
                                            ? item.getBook().getAuthor()
                                            : null);

                            dto.setQuantity(
                                    item.getQuantity());

                            dto.setPrice(
                                    item.getUnitPrice());

                            dto.setTotal(
                                    item.getUnitPrice() != null
                                            ? item.getUnitPrice()
                                            .multiply(
                                                    BigDecimal.valueOf(
                                                            item.getQuantity()))
                                            : null);

                            return dto;
                        })
                        .collect(Collectors.toList());

        response.setItems(responseItems);

        AddressResponseDto addressDto =
                new AddressResponseDto(
                        order.getShippingAddress() != null
                                ? order.getShippingAddress().getId()
                                : null,

                        order.getShippingAddress() != null
                                ? order.getShippingAddress().getCity()
                                : null,

                        order.getShippingAddress() != null
                                ? order.getShippingAddress().getState()
                                : null,

                        order.getShippingAddress() != null
                                ? order.getShippingAddress().getCountry()
                                : null);

        response.setShippingAddress(addressDto);

        return response;
    }
}
