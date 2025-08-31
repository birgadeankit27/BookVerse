package com.bookverser.BookVerse.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class BookDto {
    private Long id;
    private String title;
    private String author;
    private String description;
    private String isbn;
    private BigDecimal price;
    private Integer stock;
    private String condition;
    private String imageUrl;
    private String status;
    private Long sellerId;
    private Long categoryId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
