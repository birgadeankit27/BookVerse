package com.bookverser.BookVerse.dto;


import lombok.Data;
import java.math.BigDecimal;

@Data
public class BookSearchDto {
    private Long id;
    private String title;
    private String author;
    private BigDecimal price;
    private String categoryName;
    private String status;
}
