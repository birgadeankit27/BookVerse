package com.bookverser.BookVerse.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {
	 @NotBlank(message = "Name is required")
	    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
	    private String name;

	    @Pattern(regexp = "^[0-9]{10,14}$", message = "Phone number must be between 10 and 14 digits")
	    private String phone;

	    @Size(max = 255, message = "Address must not exceed 255 characters")
	    private String address;

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
