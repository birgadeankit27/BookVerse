package com.bookverser.BookVerse.dto;

import java.util.List;

import lombok.Data;

@Data
public class UserResponseDto {
    private Long id;
    private String name;
    private String email;
    private String role;
    private String status;
    private List<AddressResponseDto> addresses;
}
