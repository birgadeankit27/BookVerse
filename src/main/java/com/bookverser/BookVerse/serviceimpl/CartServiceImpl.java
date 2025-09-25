package com.bookverser.BookVerse.serviceimpl;
import java.math.BigDecimal;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bookverser.BookVerse.dto.AddToCartRequest;
import com.bookverser.BookVerse.dto.CartItemDto;
import com.bookverser.BookVerse.dto.CartResponseDto;
import com.bookverser.BookVerse.dto.CheckoutRequest;
import com.bookverser.BookVerse.dto.OrderResponseDto;
import com.bookverser.BookVerse.dto.UpdateCartRequest;
import com.bookverser.BookVerse.entity.Book;
import com.bookverser.BookVerse.entity.Cart;
import com.bookverser.BookVerse.entity.CartItem;
import com.bookverser.BookVerse.entity.Order;
import com.bookverser.BookVerse.entity.OrderItem;
import com.bookverser.BookVerse.entity.User;
import com.bookverser.BookVerse.exception.BookNotFoundException;
import com.bookverser.BookVerse.exception.CartItemNotFoundException;
import com.bookverser.BookVerse.exception.EmptyCartException;
import com.bookverser.BookVerse.exception.InsufficientStockException;
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

    @Override
    public CartResponseDto addToCart(Long customerId, AddToCartRequest request) {
     // Load customer
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        // ✅ Load book
        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new BookNotFoundException("Book not found with id: " + request.getBookId()));

        // ✅ Validate stock
        if (request.getQuantity() < 1 || request.getQuantity() > book.getStock()) {
            throw new InvalidQuantityException("Invalid quantity. Must be between 1 and " + book.getStock());
        }

        // ✅ Find or create cart
        Cart cart = cartRepository.findByCustomer(customer)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setCustomer(customer);
                    return cartRepository.save(newCart);
                });

        // ✅ Find or create CartItem
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

        // ✅ Update quantity
        cartItem.setQuantity(request.getQuantity());

        // ✅ Update cart total using BigDecimal
        BigDecimal newTotal = cart.getCartItems().stream()
                .map(item -> {
                    BigDecimal price = item.getBook().getPrice(); // ensure Book.price is BigDecimal
                    return price.multiply(BigDecimal.valueOf(item.getQuantity()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        cart.setTotalPrice(newTotal);

        cartRepository.save(cart); // cascade saves items too

        // ✅ Map full cart to DTO
        return CartResponseDto.builder()
                .cartId(cart.getId())
                .customerId(customer.getId())
                .totalPrice(cart.getTotalPrice()) // BigDecimal
                .items(
                        cart.getCartItems().stream()
                                .map(item -> {
                                    BigDecimal price = item.getBook().getPrice(); // BigDecimal
                                    BigDecimal total = price.multiply(BigDecimal.valueOf(item.getQuantity()));

                                    return CartItemDto.builder()
                                            .id(item.getId())
                                            .bookId(item.getBook().getId())
                                            .author(item.getBook().getAuthor())
                                            .title(item.getBook().getTitle())
                                            .quantity(item.getQuantity())
                                            .price(price)   // ✅ BigDecimal
                                            .total(total)   // ✅ BigDecimal
                                            .build();
                                })
                                .toList()
                )
                .build();
    }
    
    //Delete Book From the cart
    @Autowired
    private ModelMapper modelMapper;
	@Override
	public CartResponseDto removeCartItem(Long customerId, Long bookId) {
	    // 🔹 Load customer
	    User customer = userRepository.findById(customerId)
	            .orElseThrow(() -> new RuntimeException("Customer not found"));

	    // 🔹 Find customer's cart
	    Cart cart = cartRepository.findByCustomer(customer)
	            .orElseThrow(() -> new CartItemNotFoundException("Cart not found for customer: " + customerId));

	    // 🔹 Find the CartItem by bookId
	    CartItem cartItem = cart.getCartItems().stream()
	            .filter(item -> item.getBook().getId().equals(bookId))
	            .findFirst()
	            .orElseThrow(() -> new CartItemNotFoundException("Cart item not found for book: " + bookId));

	    // 🔹 Remove the item
	    cart.getCartItems().remove(cartItem);
	    cartItem.setCart(null); // orphanRemoval ensures deletion

	    // 🔹 Recalculate total
	    BigDecimal newTotal = cart.getCartItems().stream()
	            .map(item -> item.getBook().getPrice()
	                    .multiply(BigDecimal.valueOf(item.getQuantity())))
	            .reduce(BigDecimal.ZERO, BigDecimal::add);

	    cart.setTotalPrice(newTotal);

	    // 🔹 Save updated cart
	    cartRepository.save(cart);

	    // 🔹 Map entity → DTO using ModelMapper
	    CartResponseDto responseDto = modelMapper.map(cart, CartResponseDto.class);

	    // 🔹 Handle nested CartItems manually (to calculate total per item)
	    responseDto.setItems(
	            cart.getCartItems().stream()
	                    .map(item -> {
	                        CartItemDto dto = modelMapper.map(item, CartItemDto.class);
	                        BigDecimal price = item.getBook().getPrice();
	                        dto.setPrice(price);
	                        dto.setTotal(price.multiply(BigDecimal.valueOf(item.getQuantity())));
	                        return dto;
	                    })
	                    .toList()
	    );

	    return responseDto;
	}
	

	
	

	@Override
	public CartResponseDto getCartItems(Long buyerId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CartResponseDto clearCart(Long customerId) {
		return null;
    }
	

	@Override
	public Object checkoutCart(Long customerId, CheckoutRequest request) {
	 return null;
	}





	@Override
	public CartResponseDto updateCartItem(Long customerId, Long bookId, UpdateCartRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	
}
