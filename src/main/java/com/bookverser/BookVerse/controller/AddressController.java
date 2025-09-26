package com.bookverser.BookVerse.controller;

import com.bookverser.BookVerse.dto.AddressRequestDto;
import com.bookverser.BookVerse.dto.AddressResponseDto;
import com.bookverser.BookVerse.service.AddressService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/addresses")
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @PostMapping("/add")
    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
     public ResponseEntity<AddressResponseDto> addAddress(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody AddressRequestDto dto) {

        return ResponseEntity.ok(addressService.addAddress(userDetails.getUsername(), dto));
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
    public ResponseEntity<AddressResponseDto> updateAddress(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody AddressRequestDto dto) {

        return ResponseEntity.ok(addressService.updateAddress(userDetails.getUsername(), id, dto));
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
    public ResponseEntity<String> deleteAddress(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {

        addressService.deleteAddress(userDetails.getUsername(), id);
        return ResponseEntity.ok("Address deleted successfully");
    }

    @GetMapping("/getAddress")
    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
    public ResponseEntity<List<AddressResponseDto>> getAddresses(
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(addressService.getAddresses(userDetails.getUsername()));
    }
}
