package com.bookverser.BookVerse.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class StockUpdateDto {
    @Min(0)
    private Integer stock;
}
