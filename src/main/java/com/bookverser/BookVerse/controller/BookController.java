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
import com.bookverser.BookVerse.dto.UpdateStockRequestDTO;
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
	public ResponseEntity<?> addBook(@Valid @RequestBody CreateBookRequestDTO request, Authentication authentication) {

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

	@GetMapping("/{bookId}")
	public ResponseEntity<BookDto> getBookById(@PathVariable Long bookId) {
		BookDto bookdto = bookServiceImpl.getBookById(bookId);
		return ResponseEntity.ok(bookdto);
	}
	
	@PatchMapping("/{bookId}/stock")
	public ResponseEntity<BookDto> updateStock(@PathVariable Long bookId,
			@RequestBody @Valid UpdateStockRequestDTO request) {
		BookDto bookdto = bookServiceImpl.updateStock(bookId, request);
		return ResponseEntity.ok(bookdto);
	}


}
