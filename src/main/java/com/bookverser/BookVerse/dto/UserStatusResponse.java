package com.bookverser.BookVerse.dto;

import lombok.Data;

@Data
public class UserStatusResponse {
    private Long id;
    private String name;
    private String email;
    private String status;
}
