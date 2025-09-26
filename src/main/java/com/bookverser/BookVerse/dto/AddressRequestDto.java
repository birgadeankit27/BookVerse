package com.bookverser.BookVerse.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressRequestDto {
    @NotBlank
    private String city;
    @NotBlank
    private String state;
    @NotBlank
    private String country;
}
