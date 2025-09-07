package com.bookverser.BookVerse.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * BulkImportBookRequestDTO
 * DTO for bulk importing books (used in bulkImportBooks).
 * Represents a single book entry in the import file.
 */
@Data
public class BulkImportBookRequestDTO {

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

    @NotNull(message = "Category ID is mandatory")
    private Long categoryId;
}