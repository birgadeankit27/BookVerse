package com.bookverser.BookVerse.serviceimpl;

import com.bookverser.BookVerse.dto.*;
import com.bookverser.BookVerse.entity.*;
import com.bookverser.BookVerse.exception.*;
import com.bookverser.BookVerse.repository.*;
import com.bookverser.BookVerse.security.CustomUserDetails;
import com.bookverser.BookVerse.service.OrderService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private BookRepository bookRepository;

	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private ModelMapper modelMapper;

	@Transactional
	@Override
	public OrderResponseDto placeOrder(PlaceOrderRequest request) {

		//  Get authenticated user
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !auth.isAuthenticated())
			throw new UnauthorizedException("User not authenticated");

		// Cast principal to CustomUserDetails
		CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();

		// Fetch User entity from database
		User customer = userRepository.findById(userDetails.getId())
				.orElseThrow(() -> new UnauthorizedException("Customer not found"));

		//  Validate shipping address belongs to user
		if (customer.getAddress() == null || 
			    !customer.getAddress().equals(request.getShippingAddress())) {
			    throw new InvalidAddressException("Shipping address invalid or does not belong to user");
			}


		// Validate payment method
		if (!List.of("COD", "UPI", "Card", "NetBanking").contains(request.getPaymentMethod())) {
			throw new IllegalArgumentException("Invalid payment method: " + request.getPaymentMethod());
		}

		BigDecimal totalPrice = BigDecimal.ZERO;
		List<OrderItem> orderItems = new ArrayList<>();

		//  Validate items and stock
		for (PlaceOrderRequest.OrderItemRequest itemReq : request.getItems()) {
			Book book = bookRepository.findById(itemReq.getBookId())
					.orElseThrow(() -> new BookNotFoundException("Book not found: " + itemReq.getBookId()));

			if (book.getStock() < itemReq.getQuantity()) {
				throw new InsufficientStockException("Book " + book.getTitle() + " has insufficient stock");
			}

			// Deduct stock
			book.setStock(book.getStock() - itemReq.getQuantity());
			bookRepository.save(book);

			OrderItem orderItem = OrderItem.builder().book(book).seller(book.getSeller())
					.quantity(itemReq.getQuantity()).unitPrice(book.getPrice()).build();

			totalPrice = totalPrice.add(book.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity())));
			orderItems.add(orderItem);
		}

		// 5️⃣ Create order
		Order order = Order.builder().customer(customer) 
				.shippingAddress(customer.getAddress()) 
				.totalPrice(totalPrice).status(Order.Status.PENDING)
				.paymentStatus(
						request.getPaymentMethod().equals("COD") ? Order.PaymentStatus.COD : Order.PaymentStatus.PAID)
				.orderItems(new ArrayList<>()).build();

		//  Link order items
		for (OrderItem item : orderItems) {
			item.setOrder(order);
		}
		order.setOrderItems(orderItems);

		Order savedOrder = orderRepository.save(order);

		// Build response DTO
		List<CartItemDto> responseItems = new ArrayList<>();
		for (OrderItem item : savedOrder.getOrderItems()) {
			CartItemDto dto = new CartItemDto();
			dto.setBookId(item.getBook().getId());
			dto.setTitle(item.getBook().getTitle());
			dto.setAuthor(item.getBook().getAuthor());
			dto.setQuantity(item.getQuantity());
			dto.setPrice(item.getUnitPrice());
			dto.setTotal(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
			responseItems.add(dto);
		}

		OrderResponseDto response = new OrderResponseDto();
		response.setOrderId(savedOrder.getId());
		response.setCustomerId(savedOrder.getCustomer().getId()); // updated
		response.setPaymentMethod(request.getPaymentMethod());
		response.setStatus(savedOrder.getStatus().name());
		response.setTotalAmount(savedOrder.getTotalPrice().doubleValue());
		response.setItems(responseItems);

		return response;
	}
}
