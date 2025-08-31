package com.bookverser.BookVerse.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class BookRequestDto {

    @NotBlank
    private String title;

    @NotBlank
    private String author;

    private String description;

    @NotBlank
    private String isbn;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal price;

    @Min(0)
    private Integer stock;

    @NotBlank
    private String condition;

    private String imageUrl;
    private Long sellerId;
    private Long categoryId;
}
