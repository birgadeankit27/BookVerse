package com.bookverser.BookVerse.service;

import com.bookverser.BookVerse.dto.ReviewDTO;
import java.util.List;

public interface ReviewService {

    ReviewDTO addReview(Long bookId, Long customerId, int rating, String comment);

    List<ReviewDTO> getReviewsByBookId(Long bookId);

    void reportReview(Long reviewId, Long userId);
}
