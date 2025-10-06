package com.bookverser.BookVerse.controller;

import com.bookverser.BookVerse.dto.ReviewDTO;
import com.bookverser.BookVerse.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<ReviewDTO> addReview(
            @RequestParam Long bookId,
            @RequestParam Long customerId,
            @RequestParam int rating,
            @RequestParam(required = false) String comment
    ) {
        ReviewDTO review = reviewService.addReview(bookId, customerId, rating, comment);
        return new ResponseEntity<>(review, HttpStatus.CREATED);
    }
}
