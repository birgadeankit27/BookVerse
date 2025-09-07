package com.bookverser.BookVerse.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * UserDTO
 * Data Transfer Object for User responses.
 * Hides sensitive fields like password.
 */
@Data
public class UserDto {

    private Long id;

    @NotBlank
    @Size(min = 2, max = 100)
    private String name;

    @NotBlank
    @Email
    private String email;

    @NotNull
    private String role; // BUYER / SELLER / ADMIN

    @Size(max = 255)
    private String address;

    @Pattern(regexp = "^\\+?[0-9]{10,15}$")
    private String phone;
}