package com.bookverser.BookVerse.controller;

import java.io.IOException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.bookverser.BookVerse.dto.BookDto;
import com.bookverser.BookVerse.service.BookService;

@RestController
@RequestMapping("/books")

public class BookController {

	private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }
	
	@PostMapping
	public String addBook(@RequestBody BookDto book) {
		return null;
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
