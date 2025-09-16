package com.bookverser.BookVerse.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class UpdateCartRequest {
    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;
}
