package com.bookverser.BookVerse.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.bookverser.BookVerse.dto.BookDto;
import com.bookverser.BookVerse.dto.CreateBookRequestDTO;
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
	
	@GetMapping("/category/{categoryName}")
    public ResponseEntity<List<BookDto>> getBooksByCategory(@PathVariable String categoryName) {
        List<BookDto> books = bookServiceImpl.getBooksByCategory(categoryName);
        return ResponseEntity.ok(books);
    }
	
	@GetMapping("/search")
    public ResponseEntity<List<BookDto>> searchBooks(
            @RequestParam String keyword,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice) {

        List<BookDto> books = bookServiceImpl.searchBooks(keyword, minPrice, maxPrice);

        if (books.isEmpty()) {
            return ResponseEntity.noContent().build(); 
        }
        return ResponseEntity.ok(books); 
    }
	
	@GetMapping("/search/details")
	public ResponseEntity<List<BookDto>> searchBooksByDetails(
	        @RequestParam(required = false) String title,
	        @RequestParam(required = false) String author,
	        @RequestParam(required = false) String isbn) {

	    List<BookDto> books = bookServiceImpl.searchBooks(title, author, isbn);

	    if (books.isEmpty()) {
	        return ResponseEntity.noContent().build();
	    }
	    return ResponseEntity.ok(books);
	}
	
	 @GetMapping("/search/title")
	    public ResponseEntity<List<BookDto>> searchByTitle(@RequestParam String title) {
	        return ResponseEntity.ok(bookServiceImpl.searchBooksByTitle(title));
	    }
	 
	 @GetMapping("/search/author")
	    public ResponseEntity<List<BookDto>> searchByAuthor(@RequestParam String author) {
	        return ResponseEntity.ok(bookServiceImpl.searchBooksByAuthor(author));
	    }

	 @GetMapping("/search/category")
	    public ResponseEntity<List<BookDto>> searchByCategory(@RequestParam String category) {
	        return ResponseEntity.ok(bookServiceImpl.searchBooksByCategory(category));
	    }

	    @GetMapping("/search/isbn")
	    public ResponseEntity<BookDto> getBookByIsbn(@RequestParam String isbn) {
	        BookDto book = bookServiceImpl.searchBookByIsbn(isbn);
	        return ResponseEntity.ok(book);  
	    }

	
	@GetMapping("/filter")
	public ResponseEntity<List<BookDto>> filterBooks(
	        @RequestParam(required = false) String category,
	        @RequestParam(required = false) Double minPrice,
	        @RequestParam(required = false) Double maxPrice,
	        @RequestParam(required = false) String location) {

	    List<BookDto> books = bookServiceImpl.filterBooks(category, minPrice, maxPrice, location);

	    if (books.isEmpty()) {
	        return ResponseEntity.noContent().build();
	    }
	    return ResponseEntity.ok(books);
	}


}
