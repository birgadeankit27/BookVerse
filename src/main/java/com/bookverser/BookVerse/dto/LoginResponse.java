package com.bookverser.BookVerse.dto;

import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
    private String accessToken;      // Short-lived JWT
     private Long userId;             // User ID
    private String email;            // Email of the user
    private String name;             // User's full name
    private List<String> roles;      // Roles like CUSTOMER, SELLER, ADMIN
}
