package com.bookverser.BookVerse.dto;


import lombok.Data;

import java.math.BigDecimal;

import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
/**
 * SearchBooksRequestDTO
 * DTO for searching books (used in searchBooks).
 * Supports keyword and price range filters.
 */
@Data
public class SearchBooksRequestDTO {

    @Size(max = 255, message = "Keyword must be less than 255 characters")
    private String keyword;

    @PositiveOrZero(message = "Minimum price cannot be negative")
    private BigDecimal minPrice;

    @PositiveOrZero(message = "Maximum price cannot be negative")

  
    
    private String title;
    private String author;
    private String category;
    private String isbn;
    private String status;

    private BigDecimal maxPrice;

}