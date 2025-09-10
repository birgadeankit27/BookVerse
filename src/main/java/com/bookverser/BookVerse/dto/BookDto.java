package com.bookverser.BookVerse.dto;



import jakarta.validation.constraints.*;
import lombok.Data;


/**
 * BookDTO
 * DTO for Book responses, used in get, update, and feature operations.
 * Includes all fields needed for API responses.
 */
@Data
public class BookDto {

    private Long id;

    @NotBlank(message = "Title is mandatory")
    @Size(max = 255, message = "Title must be less than 255 characters")
    private String title;

    @NotBlank(message = "Author is mandatory")
    @Size(max = 255, message = "Author must be less than 255 characters")
    private String author;

    @Size(max = 1000, message = "Description must be less than 1000 characters")
    private String description;

    @NotNull(message = "Price is mandatory")
    @Positive(message = "Price must be positive")
    private Double price;

    @NotBlank(message = "ISBN is mandatory")
    @Pattern(regexp = "^(?:ISBN(?:-1[03])?:? )?[0-9]{10,13}$", message = "Invalid ISBN format")
    private String isbn;

    @NotNull(message = "Stock is mandatory")
    @Min(value = 0, message = "Stock cannot be negative")
    private Integer stock;

    @NotNull(message = "Condition is mandatory")
    @Pattern(regexp = "NEW|GOOD|OLD", message = "Condition must be NEW, GOOD, or OLD")
    private String condition;

    private String imageUrl;

    @NotNull(message = "Status is mandatory")
    @Pattern(regexp = "AVAILABLE|SOLD", message = "Status must be AVAILABLE or SOLD")
    private String status;

    @NotNull(message = "Seller ID is mandatory")
    private Long sellerId;

    @NotNull(message = "Category ID is mandatory")
    private Long categoryId;

    private boolean isFeatured; // For Feature Book operation
    
   
    }

