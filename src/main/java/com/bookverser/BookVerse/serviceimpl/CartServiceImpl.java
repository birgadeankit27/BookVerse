package com.bookverser.BookVerse.serviceimpl;

import com.bookverser.BookVerse.dto.AddToCartRequest;
import com.bookverser.BookVerse.dto.CartItemDto;
import com.bookverser.BookVerse.dto.CartResponseDto;
import com.bookverser.BookVerse.dto.CheckoutRequest;
import com.bookverser.BookVerse.dto.UpdateCartRequest;
import com.bookverser.BookVerse.entity.Book;
import com.bookverser.BookVerse.entity.Cart;
import com.bookverser.BookVerse.entity.CartItem;
import com.bookverser.BookVerse.entity.Role;
import com.bookverser.BookVerse.entity.User;
import com.bookverser.BookVerse.exception.BookNotFoundException;
import com.bookverser.BookVerse.exception.InvalidQuantityException;
import com.bookverser.BookVerse.exception.ResourceNotFoundException;
import com.bookverser.BookVerse.exception.UnauthorizedException;
import com.bookverser.BookVerse.repository.BookRepository;
import com.bookverser.BookVerse.repository.CartItemRepository; // Corrected repository type
import com.bookverser.BookVerse.repository.CartRepository;
import com.bookverser.BookVerse.repository.UserRepository;
import com.bookverser.BookVerse.security.CustomUserDetails;
import com.bookverser.BookVerse.service.CartService;
import org.springframework.transaction.annotation.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository; // Corrected to CartItemRepository

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public CartResponseDto removeCartItem(Long buyerId, Long bookId) {
        // TODO: Implement
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public CartResponseDto getCartItems(Long buyerId) {
        // Verify authenticated buyer (ownership check)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new UnauthorizedException("User not authenticated");
        }

        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        if (!userDetails.getId().equals(buyerId)) {
            throw new UnauthorizedException("You can access only your own cart");
        }

        // Fetch buyer
        User buyer = userRepository.findById(buyerId)
                .orElseThrow(() -> new ResourceNotFoundException("Buyer not found with id: " + buyerId));

        // Try to fetch cart
        Optional<Cart> optionalCart = cartRepository.findByCustomerId(buyerId);

        if (optionalCart.isEmpty()) {
            // Cart अजून create झालेला नाही → Empty cart response
            return CartResponseDto.builder()
                    .cartId(null)              // अजून DB मध्ये नाही
                    .buyerId(buyerId)
                    .items(Collections.emptyList())
                    .totalPrice(BigDecimal.ZERO)
                    .build();
        }

        Cart cart = optionalCart.get();

        // Fetch cart items
        List<CartItem> cartItems = cartItemRepository.findAllByCartId(cart.getId());

        // Map to DTOs
        List<CartItemDto> cartItemDtos = cartItems.stream()
                .map(item -> CartItemDto.builder()
                        .id(item.getId())
                        .bookId(item.getBook().getId())
                        .title(item.getBook().getTitle())
                        .author(item.getBook().getAuthor())
                        .price(BigDecimal.valueOf(item.getBook().getPrice()))
                        .quantity(item.getQuantity())
                        .subtotal(item.getSubtotal())
                        .build())
                .collect(Collectors.toList());

        // Calculate total price
        BigDecimal total = cartItems.stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponseDto.builder()
                .cartId(cart.getId())
                .buyerId(buyerId)
                .items(cartItemDtos)
                .totalPrice(total)
                .build();
    }



    @Override
    public CartResponseDto clearCart(Long buyerId) {
        // TODO: Implement
        return null;
    }

    @Override
    public Object checkoutCart(Long buyerId, CheckoutRequest request) {
        // TODO: Implement
        return null;
    }

    @Override
    public CartResponseDto updateCartItem(Long buyerId, Long bookId, UpdateCartRequest request) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    @Transactional
    public CartItemDto addToCart(Long customerId, AddToCartRequest request) {
    	 return null;
    }
    }
    
