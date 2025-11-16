package com.bookverser.BookVerse.service;
import com.bookverser.BookVerse.dto.ReviewDto;
import java.util.List;
public interface ReviewService {

       ReviewDto addReview(Long bookId, Long buyerId, int rating, String comment);

      List<ReviewDto> getReviewsByBookId(Long bookId);

       void reportReview(Long reviewId, Long userId);
}
