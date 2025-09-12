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
    private String role; // CUSTOMER / SELLER / ADMIN

    @Size(max = 255)
    private String address;

    @Pattern(regexp = "^\\+?[0-9]{10,15}$")
    private String phone;

    @NotBlank(message = "City is required")
    @Size(max = 50, message = "City must not exceed 50 characters")
    private String city;

    @NotBlank(message = "State is required")
    @Size(max = 50, message = "State must not exceed 50 characters")
    private String state;

    @NotBlank(message = "Country is required")
    @Size(max = 50, message = "Country must not exceed 50 characters")
    private String country;
}
