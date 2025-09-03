package com.bookverser.BookVerse.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.bookverser.BookVerse.entity.Book;

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
    
    public static BookDto fromEntity(Book book) {
        BookDto dto = new BookDto();
        dto.setId(book.getId());
        dto.setTitle(book.getTitle());
        dto.setAuthor(book.getAuthor());
        dto.setDescription(book.getDescription());
        dto.setPrice(book.getPrice());
        dto.setCondition(book.getCondition());
        dto.setImageUrl(book.getImageUrl());
        dto.setStatus(book.getStatus().name());
        dto.setSellerId(book.getSeller() != null ? book.getSeller().getId() : null);
        return dto;
    }
}
