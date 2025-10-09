package com.bookverser.BookVerse.controller;

import com.bookverser.BookVerse.dto.ReviewDTO;
import com.bookverser.BookVerse.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    /**
     * POST /api/reviews
     * Allows a customer to add a review for a purchased book.
     */
    @PostMapping
    public ResponseEntity<ReviewDTO> addReview(@RequestBody ReviewRequest request) {
        ReviewDTO review = reviewService.addReview(
                request.getBookId(),
                request.getCustomerId(),
                request.getRating(),
                request.getComment()
        );
        return ResponseEntity.status(201).body(review);
    }

    /**
     * GET /api/reviews/book/{bookId}
     * Fetches all reviews for a specific book.
     * Accessible by anyone (customer, seller, or guest).
     */
    @GetMapping("/book/{bookId}")
    public ResponseEntity<List<ReviewDTO>> getReviewsByBook(@PathVariable Long bookId) {
        List<ReviewDTO> reviews = reviewService.getReviewsByBookId(bookId);
        return ResponseEntity.ok(reviews);
    }

    // DTO for POST body
    public static class ReviewRequest {
        private Long bookId;
        private Long customerId;
        private int rating;
        private String comment;

        public Long getBookId() { return bookId; }
        public void setBookId(Long bookId) { this.bookId = bookId; }

        public Long getCustomerId() { return customerId; }
        public void setCustomerId(Long customerId) { this.customerId = customerId; }

        public int getRating() { return rating; }
        public void setRating(int rating) { this.rating = rating; }

        public String getComment() { return comment; }
        public void setComment(String comment) { this.comment = comment; }
    }
}
