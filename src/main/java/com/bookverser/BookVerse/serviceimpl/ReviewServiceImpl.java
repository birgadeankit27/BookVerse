package com.bookverser.BookVerse.serviceimpl;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.bookverser.BookVerse.dto.ReviewDTO;
import com.bookverser.BookVerse.entity.Book;
import com.bookverser.BookVerse.entity.Review;
import com.bookverser.BookVerse.entity.User;
import com.bookverser.BookVerse.exception.BookNotFoundException;
import com.bookverser.BookVerse.exception.InvalidReviewException;
import com.bookverser.BookVerse.exception.UnauthorizedException;
import com.bookverser.BookVerse.repository.BookRepository;
import com.bookverser.BookVerse.repository.OrderItemRepository;
import com.bookverser.BookVerse.repository.ReviewRepository;
import com.bookverser.BookVerse.repository.UserRepository;
import com.bookverser.BookVerse.service.ReviewService;

@Service
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final OrderItemRepository orderItemRepository;

    public ReviewServiceImpl(
            ReviewRepository reviewRepository,
            BookRepository bookRepository,
            UserRepository userRepository,
            OrderItemRepository orderItemRepository) {

        this.reviewRepository = reviewRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
        this.orderItemRepository = orderItemRepository;
    }

    @Override
    public ReviewDTO addReview(
            Long bookId,
            Long customerId,
            int rating,
            String comment) throws InvalidReviewException  {

        // 1. Validate customer
        User customer = userRepository.findById(customerId)
                .orElseThrow(() ->
                        new UnauthorizedException(
                                "Customer not found or unauthorized."));

        // 2. Validate book
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() ->
                        new BookNotFoundException(
                                "Book not found."));

        // 3. Check if customer purchased the book
        boolean hasPurchased =
                orderItemRepository.existsByCustomerAndBook(
                        customer,
                        book);

        if (!hasPurchased) {
            throw new UnauthorizedException(
                    "You must purchase this book before reviewing it.");
        }

        // 4. Validate rating
        if (rating < 1 || rating > 5) {
            throw new InvalidReviewException(
                    "Rating must be between 1 and 5.");
        }

        // 5. Validate comment
        if (comment != null && comment.length() > 500) {
            throw new InvalidReviewException(
                    "Comment must be less than 500 characters.");
        }

        // 6. Prevent duplicate reviews
        if (reviewRepository.existsByBookAndUser(book, customer)) {
            throw new InvalidReviewException(
                    "You have already reviewed this book.");
        }

        // 7. Create review
        Review review = new Review();

        review.setBook(book);
        review.setUser(customer);
        review.setRating(rating);
        review.setComment(comment);

        Review savedReview = reviewRepository.save(review);

        // 8. Convert to DTO
        ReviewDTO dto = new ReviewDTO();

        dto.setReviewId(savedReview.getId());
        dto.setBookId(book.getId());
        dto.setCustomerId(customer.getId());
        dto.setCustomerName(customer.getName());
        dto.setCustomerEmail(customer.getEmail());
        dto.setRating(savedReview.getRating());
        dto.setComment(savedReview.getComment());
        dto.setCreatedAt(savedReview.getCreatedAt());

        return dto;
    }

    @Override
    public List<ReviewDTO> getReviewsByBookId(Long bookId) {

        // Validate book
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() ->
                        new BookNotFoundException(
                                "Book not found."));

        return reviewRepository.findByBook(book)
                .stream()
                .map(review -> {

                    ReviewDTO dto = new ReviewDTO();

                    dto.setReviewId(review.getId());
                    dto.setBookId(book.getId());
                    dto.setCustomerId(review.getUser().getId());
                    dto.setCustomerName(review.getUser().getName());
                    dto.setCustomerEmail(review.getUser().getEmail());
                    dto.setRating(review.getRating());
                    dto.setComment(review.getComment());
                    dto.setCreatedAt(review.getCreatedAt());

                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public void reportReview(Long reviewId, Long userId) {
        // Optional: implement later
    }
}
