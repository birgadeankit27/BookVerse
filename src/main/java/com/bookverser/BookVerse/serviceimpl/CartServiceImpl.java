package com.bookverser.BookVerse.serviceimpl;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import com.bookverser.BookVerse.config.ModelMapperConfig;
import com.bookverser.BookVerse.dto.*;
import com.bookverser.BookVerse.entity.*;
import com.bookverser.BookVerse.exception.*;
import com.bookverser.BookVerse.repository.*;
import com.bookverser.BookVerse.service.CartService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService {

    private final BookRepository bookRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final AddressRepository addressRepository;
    private final ModelMapperConfig modelMapperConfig;
    private final ModelMapper modelMapper;

    // ====================================================
    // 🛒 1. Add to Cart
    // ====================================================
    @Override
    public CartResponseDto addToCart(Long customerId, AddToCartRequest request) {
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new BookNotFoundException("Book not found with id: " + request.getBookId()));

        if (request.getQuantity() < 1 || request.getQuantity() > book.getStock()) {
            throw new InvalidQuantityException("Invalid quantity. Must be between 1 and " + book.getStock());
        }

        Cart cart = cartRepository.findByCustomer(customer)
                .orElseGet(() -> cartRepository.save(Cart.builder().customer(customer).build()));

        CartItem cartItem = cart.getCartItems().stream()
                .filter(item -> item.getBook().getId().equals(book.getId()))
                .findFirst()
                .orElseGet(() -> {
                    CartItem newItem = new CartItem();
                    newItem.setCart(cart);
                    newItem.setBook(book);
                    cart.getCartItems().add(newItem);
                    return newItem;
                });

        cartItem.setQuantity(request.getQuantity());

        // Recalculate total
        BigDecimal newTotal = cart.getCartItems().stream()
                .map(item -> item.getBook().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        cart.setTotalPrice(newTotal);
        cartRepository.save(cart);

        // Build response
        return CartResponseDto.builder()
                .cartId(cart.getId())
                .customerId(customer.getId())
                .totalPrice(cart.getTotalPrice())
                .items(cart.getCartItems().stream().map(this::toCartItemDto).toList())
                .build();
    }

    // ====================================================
    // ❌ 2. Remove Item from Cart
    // ====================================================
    @Override
    public Map<String, Object> removeCartItem(Long customerId, Long bookId) {
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new UnauthorizedException("Customer not found"));

        Cart cart = cartRepository.findByCustomer(customer)
                .orElseThrow(() -> new CartItemNotFoundException("Cart not found for customer"));

        CartItem cartItem = cart.getCartItems().stream()
                .filter(item -> item.getBook().getId().equals(bookId))
                .findFirst()
                .orElseThrow(() -> new CartItemNotFoundException("Book with id " + bookId + " not found in cart"));

        cart.getCartItems().remove(cartItem);

        // Recalculate total
        BigDecimal newTotal = cart.getCartItems().stream()
                .map(item -> item.getBook().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        cart.setTotalPrice(newTotal);
        cartRepository.save(cart);

        // Build response
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Book removed from cart successfully");
        response.put("cartId", cart.getId());
        response.put("customerId", customer.getId());
        response.put("totalPrice", cart.getTotalPrice());
        response.put("items", cart.getCartItems().stream().map(this::toCartItemDto).toList());

        return response;
    }

    // ====================================================
    // 🔁 3. Update Cart Item Quantity
    // ====================================================
    @Override
    public CartItemDto updateCartItem(Long customerId, Long bookId, UpdateCartRequest request) {
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new UnauthorizedException("Customer not found"));

        Cart cart = cartRepository.findByCustomer(customer)
                .orElseThrow(() -> new UnauthorizedException("Cart not found for this user"));

        CartItem cartItem = cart.getCartItems().stream()
                .filter(item -> item.getBook().getId().equals(bookId))
                .findFirst()
                .orElseThrow(() -> new CartItemNotFoundException("Cart item not found for bookId: " + bookId));

        Book book = cartItem.getBook();
        int newQty = request.getQuantity();

        if (newQty < 1 || newQty > book.getStock()) {
            throw new InvalidQuantityException("Quantity must be between 1 and " + book.getStock());
        }

        cartItem.setQuantity(newQty);

        // Recalculate total
        BigDecimal newTotal = cart.getCartItems().stream()
                .map(item -> item.getBook().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        cart.setTotalPrice(newTotal);
        cartRepository.save(cart);

        return toCartItemDto(cartItem);
    }

    // ====================================================
    // 📋 4. Get Cart Items
    // ====================================================
    @Override
    public CartResponseDto getCartItems(Long customerId) {
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new UnauthorizedException("Customer not found"));

        Cart cart = cartRepository.findByCustomer(customer)
                .orElseThrow(() -> new CartItemNotFoundException("Cart not found"));

        return CartResponseDto.builder()
                .cartId(cart.getId())
                .customerId(customer.getId())
                .totalPrice(cart.getTotalPrice())
                .items(cart.getCartItems().stream().map(this::toCartItemDto).toList())
                .build();
    }

    // ====================================================
    // 🧹 5. Clear Cart
    // ====================================================
    @Override
    public CartResponseDto clearCart(Long customerId) {
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new UnauthorizedException("Customer not found"));

        Cart cart = cartRepository.findByCustomer(customer)
                .orElseThrow(() -> new UnauthorizedException("Cart not found for this user"));

        cart.getCartItems().clear();
        cart.setTotalPrice(BigDecimal.ZERO);
        cartRepository.save(cart);

        return CartResponseDto.builder()
                .cartId(cart.getId())
                .customerId(customer.getId())
                .totalPrice(BigDecimal.ZERO)
                .items(List.of())
                .build();
    }

    // ====================================================
    // ✅ 6. Checkout Cart
    // ====================================================
    @Override
    public Object checkoutCart(Long customerId, CheckoutRequest request) {
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        Cart cart = cartRepository.findByCustomer(customer)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        if (cart.getCartItems().isEmpty()) {
            throw new RuntimeException("Cart is empty. Cannot checkout.");
        }

        Address shippingAddress = addressRepository.findByIdAndUser(request.getShippingAddressId(), customer)
                .orElseThrow(() -> new RuntimeException("Address not found for this customer"));

        // Validate stock
        for (CartItem item : cart.getCartItems()) {
            if (item.getQuantity() > item.getBook().getStock()) {
                throw new RuntimeException("Insufficient stock for book: " + item.getBook().getTitle());
            }
        }

        List<OrderDTO> orders = new ArrayList<>();
        for (CartItem item : cart.getCartItems()) {
            Order order = Order.builder()
                    .customer(customer)
                    .cart(cart)
                    .shippingAddress(shippingAddress)
                    .status(Order.Status.PENDING)
                    .paymentStatus(
                            request.getPaymentMethod().equalsIgnoreCase("COD")
                                    ? Order.PaymentStatus.COD
                                    : Order.PaymentStatus.PAID)
                    .totalPrice(item.getBook().getPrice()
                            .multiply(BigDecimal.valueOf(item.getQuantity())))
                    .build();

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .book(item.getBook())
                    .seller(item.getBook().getSeller())
                    .quantity(item.getQuantity())
                    .unitPrice(item.getBook().getPrice())
                    .build();

            order.getOrderItems().add(orderItem);
            orderRepository.save(order);

            OrderDTO dto = modelMapper.map(order, OrderDTO.class);
            dto.setBuyerId(customer.getId());
            dto.setBookId(item.getBook().getId());
            dto.setSellerId(item.getBook().getSeller().getId());
            orders.add(dto);
        }

        // Clear cart
        cart.getCartItems().clear();
        cart.setTotalPrice(BigDecimal.ZERO);
        cartRepository.save(cart);

        return orders;
    }

    // ====================================================
    // 🧩 Helper Method
    // ====================================================
    private CartItemDto toCartItemDto(CartItem item) {
        BigDecimal price = item.getBook().getPrice();
        BigDecimal total = price.multiply(BigDecimal.valueOf(item.getQuantity()));

        return CartItemDto.builder()
                .id(item.getId())
                .bookId(item.getBook().getId())
                .author(item.getBook().getAuthor())
                .title(item.getBook().getTitle())
                .quantity(item.getQuantity())
                .price(price)
                .total(total)
                .build();
    }
}
