package com.bookverser.BookVerse.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressResponseDto {
    private Long id;
    private String city;
    private String state;
    private String country;
}
