package com.bookverser.BookVerse.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDTO {
    private Long reviewId;
    private Long bookId;

    // Customer info
    private Long customerId;
    private String customerName;
    private String customerEmail;

    private int rating;
    private String comment;
    private LocalDateTime createdAt;
}
