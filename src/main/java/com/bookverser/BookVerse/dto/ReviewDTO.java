package com.bookverser.BookVerse.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewDTO {
	   private Long id;
	    private Long reviewerId;
	    private Long sellerId;
	    private Long bookId;
	    private int rating;   // 1-5
	    private String comment;

}
