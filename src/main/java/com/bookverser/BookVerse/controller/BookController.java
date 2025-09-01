package com.bookverser.BookVerse.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bookverser.BookVerse.dto.BookDto;






@RestController

@RequestMapping()
@RequestMapping("books")

public class BookController {

	@PostMapping("/api/books")
	public String addBook(@RequestBody BookDto book) {
		return null;
		
	}
}
