package com.bookverser.BookVerse.dto;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor

public class LoginResponse {
	 private String token;
	    private Date   expiresAt;
	    private String email;
	    private List<String> roles;
}
