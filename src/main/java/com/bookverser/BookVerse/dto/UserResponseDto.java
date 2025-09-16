package com.bookverser.BookVerse.dto;

import lombok.Data;

@Data
public class UserResponseDto {
    private Long id;
    private String name;
    private String email;
    private String role;
    private String status;
    private String city;
    private String state;
    private String country;
}
