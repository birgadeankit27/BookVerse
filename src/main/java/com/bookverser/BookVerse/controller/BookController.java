package com.bookverser.BookVerse.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bookverser.BookVerse.dto.BookDto;
import com.bookverser.BookVerse.dto.BookRequest;
import com.bookverser.BookVerse.service.BookService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("books")
public class BookController {

	@Autowired
	private BookService service;
	
	@PostMapping("/api/books")
	public ResponseEntity<BookDto> addBook(@Valid @RequestBody BookRequest book) {
	    BookDto bookDto = service.addBook(book);
	    return ResponseEntity.ok(bookDto);
	}
}
