package com.bookverser.BookVerse.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * UpdateBookRequestDTO
 * DTO for updating a book (used in updateBook).
 * Allows partial updates, so most fields are optional except ISBN (cannot be updated).
 */
@Data
public class UpdateBookRequestDTO {

    @Size(max = 255, message = "Title must be less than 255 characters")
    private String title;

    @Size(max = 255, message = "Author must be less than 255 characters")
    private String author;

    @Size(max = 1000, message = "Description must be less than 1000 characters")
    private String description;

    @Positive(message = "Price must be positive")
    private Double price;

    @Min(value = 0, message = "Stock cannot be negative")
    private Integer stock;

    @Pattern(regexp = "NEW|GOOD|OLD", message = "Condition must be NEW, GOOD, or OLD")
    private String condition;

    @Size(max = 255, message = "Image URL must be less than 255 characters")
    private String imageUrl;

    private Long categoryId; // optional, but can be updated

    @Pattern(regexp = "AVAILABLE|UNAVAILABLE|SOLD_OUT", 
             message = "Status must be AVAILABLE, UNAVAILABLE, or SOLD_OUT")
    private String status;

    private Boolean isFeatured;

    private Boolean isActive;
}
