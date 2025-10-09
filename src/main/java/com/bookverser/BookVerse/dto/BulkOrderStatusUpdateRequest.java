package com.bookverser.BookVerse.dto;

import lombok.Data;
import java.util.List;

@Data
public class BulkOrderStatusUpdateRequest {
    private List<Long> orderIds;
    private String status;
}