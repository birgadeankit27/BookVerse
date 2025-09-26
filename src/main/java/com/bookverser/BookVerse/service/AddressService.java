package com.bookverser.BookVerse.service;

import com.bookverser.BookVerse.dto.AddressRequestDto;
import com.bookverser.BookVerse.dto.AddressResponseDto;

import java.util.List;

public interface AddressService {
    AddressResponseDto addAddress(String userEmail, AddressRequestDto dto);
    AddressResponseDto updateAddress(String userEmail, Long addressId, AddressRequestDto dto);
    void deleteAddress(String userEmail, Long addressId);
    List<AddressResponseDto> getAddresses(String userEmail);
}
