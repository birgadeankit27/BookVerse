package com.bookverser.BookVerse.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
	private Long id;
    private String name;
    private String email;
    private String role;   // BUYER / SELLER / ADMIN
    private String address;
    private String phone;

}
