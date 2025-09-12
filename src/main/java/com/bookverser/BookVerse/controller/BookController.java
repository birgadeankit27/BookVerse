package com.bookverser.BookVerse.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpStatus;
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
import com.bookverser.BookVerse.exception.DuplicateIsbnException;
import com.bookverser.BookVerse.exception.InvalidRequestException;
import com.bookverser.BookVerse.exception.UnauthorizedException;

import com.bookverser.BookVerse.dto.UpdateStockRequestDTO;

import com.bookverser.BookVerse.repository.UserRepository;
import com.bookverser.BookVerse.serviceimpl.BookServiceImpl;

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

	@GetMapping("/filter")
	public ResponseEntity<List<BookDto>> filterBooks(@RequestParam(required = false) String category,
			@RequestParam(required = false) Double minPrice, @RequestParam(required = false) Double maxPrice,
			@RequestParam(required = false) String location) {
		List<BookDto> bookDtos = bookServiceImpl.filterBooks(category, minPrice, maxPrice, location);

		if (bookDtos.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
		}

		return ResponseEntity.ok(bookDtos);
	}

	@GetMapping("/sort")
	public ResponseEntity<List<BookDto>> sortBooks(@RequestParam(required = false) String sortBy) {
		List<BookDto> bookDtos;
		try {
			bookDtos = bookServiceImpl.sortBooks(sortBy);
		} catch (InvalidRequestException e) {
			throw new InvalidRequestException("Invalid Request Exception");
		}
		return ResponseEntity.ok(bookDtos);
	}

	@GetMapping("/category/{categoryName}")
	public ResponseEntity<List<BookDto>> getBooksByCategory(@PathVariable String categoryName) {
		List<BookDto> books = bookServiceImpl.getBooksByCategory(categoryName);
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
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("File processing error: " + e.getMessage());
		}
	}

	@GetMapping("/getAll")
	public Page<BookDto> getAllBooks(@RequestParam(required = false) String category,
			@RequestParam(required = false) String author, @RequestParam(required = false) Double minPrice,
			@RequestParam(required = false) Double maxPrice, Pageable pageable) {
		return bookServiceImpl.getAllBooks(pageable, category, author, minPrice, maxPrice);
	}

	@PostMapping("/{bookId}/uploadImage")
	public BookDto uploadBookImage(@PathVariable Long bookId, @RequestParam("file") MultipartFile file)
			throws IOException {
		return bookServiceImpl.uploadImage(bookId, file);
	}
}
