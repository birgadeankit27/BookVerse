package com.bookverser.BookVerse.service;
import com.bookverser.BookVerse.dto.*;


public interface CartService {

    /**
     * Add a book to the buyer's cart.
     * @param buyerId ID of the buyer.
     * @param request AddToCartRequest containing bookId and quantity.
     * @return Updated CartResponseDto with current cart items and total price.
     */
    CartResponseDto addToCart(Long customerId, AddToCartRequest request);

    /**
     * Update the quantity of a specific book in the cart.
     * @param buyerId ID of the buyer.
     * @param bookId ID of the book to update.
     * @param request UpdateCartRequest containing new quantity.
     * @return Updated CartResponseDto.
     */
    CartResponseDto updateCartItem(Long customerId, Long bookId, UpdateCartRequest request);

    /**
     * Remove a specific book from the cart.
     * @param buyerId ID of the buyer.
     * @param bookId ID of the book to remove.
     * @return Updated CartResponseDto.
     */
    CartResponseDto removeCartItem(Long customerId ,Long bookId);

    /**
     * Retrieve all items in the buyer's cart.
     * @param buyerId ID of the buyer.
     * @return CartResponseDto containing all cart items and total price.
     */
    CartResponseDto getCartItems(Long customerId);

    /**
     * Clear all items from the buyer's cart.
     * @param buyerId ID of the buyer.
     * @return Empty CartResponseDto.
     */
    CartResponseDto clearCart(Long customerId);

    /**
     * Checkout the cart and create an order.
     * @param buyerId ID of the buyer.
     * @param request CheckoutRequest containing payment method.
     * @return Order details or confirmation (could be extended with OrderDto).
     */
    Object checkoutCart(Long customerId, CheckoutRequest request);
}
