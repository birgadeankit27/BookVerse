package com.bookverser.BookVerse.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.bookverser.BookVerse.dto.BookDto;
import com.bookverser.BookVerse.dto.CreateBookRequestDTO;
import com.bookverser.BookVerse.dto.SearchBooksRequestDTO;
import com.bookverser.BookVerse.exception.DuplicateIsbnException;
import com.bookverser.BookVerse.exception.UnauthorizedException;
import com.bookverser.BookVerse.repository.UserRepository;

import com.bookverser.BookVerse.serviceimpl.*;


import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/books")
public class BookController {

    @Autowired
    private BookServiceImpl bookServiceImpl;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/add")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public ResponseEntity<?> addBook(@Valid @RequestBody CreateBookRequestDTO request,
                                     Authentication authentication) {

    	   BookDto createdBook = bookServiceImpl.addBook(request);
           return ResponseEntity.ok(createdBook);
}
    
    
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('CUSTOMER','SELLER','ADMIN')")
    public ResponseEntity<List<BookDto>> searchBooks(@Valid SearchBooksRequestDTO request) {
        List<BookDto> books = bookServiceImpl.searchBooks(request);
        if (books.isEmpty()) {
            return ResponseEntity.noContent().build(); 
        }
        return ResponseEntity.ok(books);
    }
    
 // ------------------- Get Books by Seller -------------------
    @GetMapping("/seller/{sellerId}")
    @PreAuthorize("hasAnyRole('SELLER','ADMIN')")
    public ResponseEntity<List<BookDto>> getBooksBySeller(@PathVariable Long sellerId) {
        List<BookDto> books = bookServiceImpl.getBooksBySeller(sellerId);
        return ResponseEntity.ok(books);
    }
    // ------------------- Bulk Import Books (Admin only) -------------------
    @PostMapping("/admin/bulk-import")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> bulkImportBooks(@RequestParam("file") MultipartFile file) throws java.io.IOException {
        try {
            bookServiceImpl.bulkImportBooks(file);
            return ResponseEntity.ok("✅ Books imported successfully!");
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (DuplicateIsbnException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("File processing error: " + e.getMessage());
        }
    }

}
