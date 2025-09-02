package com.bookverser.BookVerse.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.bookverser.BookVerse.dto.BookDto;
import com.bookverser.BookVerse.dto.BookRequest;
import com.bookverser.BookVerse.service.BookService;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookController {

    @Autowired
    private BookService service;

    @PostMapping("/addBook")
    public ResponseEntity<BookDto> addBook(@Valid @RequestBody BookRequest book) {
        BookDto bookDto = service.addBook(book);
        return ResponseEntity.ok(bookDto);
    }

    @GetMapping("/getBooks")
    public ResponseEntity<List<BookDto>> getBooks() {
        // Extract the list from the Page returned by service
        List<BookDto> books = service.getAllBooks(null, null, null, null, null).getContent();
        return ResponseEntity.ok(books);
    }
}
