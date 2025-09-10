package com.bookverser.BookVerse.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.bookverser.BookVerse.dto.BookDto;
import com.bookverser.BookVerse.dto.CreateBookRequestDTO;
import com.bookverser.BookVerse.dto.SearchBooksRequestDTO;
import com.bookverser.BookVerse.repository.UserRepository;
import com.bookverser.BookVerse.service.BookService;
import com.bookverser.BookVerse.serviceimpl.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/books")
public class BookController {
	
	private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

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
    
	@GetMapping("/getAll")
	public Page<BookDto> getAllBooks(
	        @RequestParam(required = false) String category,
	        @RequestParam(required = false) String author,
	        @RequestParam(required = false) Double minPrice,
	        @RequestParam(required = false) Double maxPrice,
	        Pageable pageable) {
	    return bookService.getAllBooks(pageable, category, author, minPrice, maxPrice);
	}

	@PostMapping("/{bookId}/uploadImage")
	public BookDto uploadBookImage(
	        @PathVariable Long bookId,
	        @RequestParam("file") MultipartFile file) throws IOException {
	    return bookService.uploadImage(bookId, file);
	}
}
