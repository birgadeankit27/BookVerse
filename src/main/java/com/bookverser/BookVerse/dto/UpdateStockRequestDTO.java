package com.bookverser.BookVerse.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * UpdateStockRequestDTO
 * DTO for updating book stock (used in updateStock).
 * Ensures atomic updates with optimistic locking in service layer.
 */
@Data
public class UpdateStockRequestDTO {

    @NotNull(message = "Stock is mandatory")
    @Min(value = 0, message = "Stock cannot be negative")
    private Integer stock;
}
