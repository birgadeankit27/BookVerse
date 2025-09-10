package com.bookverser.BookVerse.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.bookverser.BookVerse.dto.BookDto;
import com.bookverser.BookVerse.dto.CreateBookRequestDTO;
import com.bookverser.BookVerse.dto.SearchBooksRequestDTO;
import com.bookverser.BookVerse.dto.UpdateBookRequestDTO;
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
    
    
    
    @PutMapping("/{bookId}")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public ResponseEntity<BookDto> updateBook(
            @PathVariable Long bookId,
            @Valid @RequestBody UpdateBookRequestDTO request) {

        BookDto updatedBook = bookServiceImpl.updateBook(bookId, request);
        return ResponseEntity.ok(updatedBook); 
    }
    
    
    @DeleteMapping("/{bookId}")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public ResponseEntity<Void> deleteBook(@PathVariable Long bookId) {
        bookServiceImpl.deleteBook(bookId);
        return ResponseEntity.noContent().build(); 
    }

}
