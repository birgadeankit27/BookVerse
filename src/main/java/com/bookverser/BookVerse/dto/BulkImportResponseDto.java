package com.bookverser.BookVerse.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BulkImportResponseDto {
    private int totalRecords;
    private int successfulImports;
    private int failedImports;
    private String message;
}
