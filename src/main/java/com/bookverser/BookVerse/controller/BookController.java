package com.bookverser.BookVerse.controller;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bookverser.BookVerse.dto.BookDto;
import com.bookverser.BookVerse.serviceimpl.BookServiceImpl;

@RestController
@RequestMapping("/api/books/")
public class BookController {
	
	@Autowired
	BookServiceImpl serviceimpl;
	
	@GetMapping("/search")
	 ResponseEntity bookSearch(@RequestParam String keyword,@RequestParam Double minPrice, @RequestParam Double maxPrice){
		 List<BookDto> BookDto = serviceimpl.searchBooks(keyword, minPrice, maxPrice);
		 if(BookDto.isEmpty()) {
			 return new ResponseEntity(HttpStatus.NO_CONTENT);
		 }
		return new ResponseEntity(BookDto, HttpStatus.OK);

		  
	  }
	
	

}
