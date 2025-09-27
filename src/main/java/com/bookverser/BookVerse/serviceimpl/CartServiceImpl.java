package com.bookverser.BookVerse.serviceimpl;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import com.bookverser.BookVerse.dto.AddToCartRequest;
import com.bookverser.BookVerse.dto.CartItemDto;
import com.bookverser.BookVerse.dto.CartResponseDto;
import com.bookverser.BookVerse.dto.CheckoutRequest;
import com.bookverser.BookVerse.dto.UpdateCartRequest;
import com.bookverser.BookVerse.entity.Book;
import com.bookverser.BookVerse.entity.Cart;
import com.bookverser.BookVerse.entity.CartItem;
import com.bookverser.BookVerse.entity.User;
import com.bookverser.BookVerse.exception.BookNotFoundException;
import com.bookverser.BookVerse.exception.CartItemNotFoundException;
import com.bookverser.BookVerse.exception.InvalidQuantityException;
import com.bookverser.BookVerse.exception.UnauthorizedException;
import com.bookverser.BookVerse.repository.BookRepository;
import com.bookverser.BookVerse.repository.CartRepository;
import com.bookverser.BookVerse.repository.OrderRepository;
import com.bookverser.BookVerse.repository.UserRepository;
import com.bookverser.BookVerse.service.CartService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final BookRepository bookRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final ModelMapper modelMapper;

    /**
     * Add book to cart
     */
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
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setCustomer(customer);
                    return cartRepository.save(newCart);
                });

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

        BigDecimal newTotal = cart.getCartItems().stream()
                .map(item -> item.getBook().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        cart.setTotalPrice(newTotal);
        cartRepository.save(cart);

        return CartResponseDto.builder()
                .cartId(cart.getId())
                .customerId(customer.getId())
                .totalPrice(cart.getTotalPrice())
                .items(
                        cart.getCartItems().stream()
                                .map(item -> {
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
                                })
                                .toList()
                )
                .build();
    }

    /**
     * Remove item from cart
     */
    @Override
    public Map<String, Object> removeCartItem(Long customerId, Long bookId) {
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new UnauthorizedException("Customer not found"));

        Cart cart = cartRepository.findByCustomer(customer)
                .orElseThrow(() -> new CartItemNotFoundException("Cart not found for customer"));

        CartItem cartItem = cart.getCartItems().stream()
                .filter(item -> item.getBook().getId().equals(bookId))
                .findFirst()
                .orElseThrow(() -> new CartItemNotFoundException(
                        "Book with id " + bookId + " not found in cart"));

        cart.getCartItems().remove(cartItem);

        BigDecimal newTotal = cart.getCartItems().stream()
                .map(item -> item.getBook().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        cart.setTotalPrice(newTotal);
        cartRepository.save(cart);

        List<CartItemDto> itemsDto = cart.getCartItems().stream()
                .map(item -> modelMapper.map(item, CartItemDto.class))
                .toList();

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Book removed from cart successfully");
        response.put("cartId", cart.getId());
        response.put("customerId", customer.getId());
        response.put("totalPrice", cart.getTotalPrice());
        response.put("items", itemsDto);

        return response;
    }

    /**
     * Update cart item quantity
     */
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

        BigDecimal newTotal = cart.getCartItems().stream()
                .map(item -> item.getBook().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        cart.setTotalPrice(newTotal);
        cartRepository.save(cart);

        BigDecimal price = book.getPrice();
        BigDecimal total = price.multiply(BigDecimal.valueOf(cartItem.getQuantity()));

        return CartItemDto.builder()
                .id(cartItem.getId())
                .bookId(book.getId())
                .author(book.getAuthor())
                .title(book.getTitle())
                .quantity(cartItem.getQuantity())
                .price(price)
                .total(total)
                .build();
    }

    /**
     * Get cart items
     */
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
                .items(
                        cart.getCartItems().stream()
                                .map(item -> {
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
                                })
                                .toList()
                )
                .build();
    }

    /**
     * Clear cart
     */
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
                .totalPrice(cart.getTotalPrice())
                .items(List.of())
                .build();
    }

    /**
     * Checkout (not implemented yet)
     */
    @Override
    public Object checkoutCart(Long customerId, CheckoutRequest request) {
        return null; // implement later
    }
}
