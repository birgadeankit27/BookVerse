package com.bookverser.BookVerse.serviceimpl;

import com.bookverser.BookVerse.dto.ReviewDTO;
import com.bookverser.BookVerse.entity.Book;
import com.bookverser.BookVerse.entity.User;
import com.bookverser.BookVerse.entity.Review;
import com.bookverser.BookVerse.exception.BookNotFoundException;
import com.bookverser.BookVerse.exception.InvalidReviewException;
import com.bookverser.BookVerse.exception.UnauthorizedException;
import com.bookverser.BookVerse.repository.BookRepository;
import com.bookverser.BookVerse.repository.ReviewRepository;
import com.bookverser.BookVerse.repository.UserRepository;
import com.bookverser.BookVerse.repository.OrderItemRepository;
import com.bookverser.BookVerse.service.ReviewService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewServiceImpl implements ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public ReviewDTO addReview(Long bookId, Long customerId, int rating, String comment) {
        // 1️⃣ Validate customer
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new UnauthorizedException("Customer not found or unauthorized."));

        // 2️⃣ Validate book
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException("Book not found."));

        // 3️⃣ Check if customer purchased the book
        boolean hasPurchased = orderItemRepository.existsByCustomerAndBook(customer, book);
        if (!hasPurchased) {
            throw new UnauthorizedException("You must purchase this book before reviewing it.");
        }

        // 4️⃣ Validate rating
        if (rating < 1 || rating > 5) {
            throw new InvalidReviewException("Rating must be between 1 and 5.");
        }

        // 5️⃣ Validate comment
        if (comment != null && comment.length() > 500) {
            throw new InvalidReviewException("Comment must be less than 500 characters.");
        }

        // 6️⃣ Prevent duplicate reviews
        if (reviewRepository.existsByBookAndUser(book, customer)) {
            throw new InvalidReviewException("You have already reviewed this book.");
        }

        // 7️⃣ Save review
        Review review = new Review();
        review.setBook(book);
        review.setUser(customer);
        review.setRating(rating);
        review.setComment(comment);

        Review saved = reviewRepository.save(review);

        // 8️⃣ Convert to DTO
        ReviewDTO dto = new ReviewDTO();
        dto.setReviewId(saved.getId());
        dto.setBookId(book.getId());
        dto.setCustomerId(customer.getId());
        dto.setCustomerName(customer.getName());
        dto.setCustomerEmail(customer.getEmail());
        dto.setRating(saved.getRating());
        dto.setComment(saved.getComment());
        dto.setCreatedAt(saved.getCreatedAt());

        return dto;
    }

    @Override
    public List<ReviewDTO> getReviewsByBookId(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException("Book not found."));

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
