package com.bookverser.BookVerse.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for Review
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDto {

    private Long reviewId;

    private Long bookId;

    // Customer info – adjust fields based on your Customer/User entity
    private Long customerId;
    private String customerName;
    private String customerEmail;

    private int rating;
    private String comment;
    private LocalDateTime createdAt;
}
