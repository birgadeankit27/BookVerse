package com.bookverser.BookVerse.serviceimpl;
import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.bookverser.BookVerse.dto.BookDto;
import com.bookverser.BookVerse.dto.BookRequestDto;

import com.bookverser.BookVerse.repository.BookRepository;
import com.bookverser.BookVerse.service.BookService;


@Service
public class BookServiceImpl implements BookService {
	
	@Autowired

	BookRepository bookrepository;

	private BookRepository bookRepository;


	@Override
	public BookDto addBook(BookRequestDto request) {
		
		return null;
	}

	@Override
	public Page<BookDto> getAllBooks(Pageable pageable, String category, String author, Double minPrice,
			Double maxPrice) {
		
		return null;
	}

	@Override
	public BookDto getBookById(Long bookId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BookDto updateBook(Long bookId, BookRequestDto request) {
		System.out.print("this is service")
		return null;
	}

	@Override
	public void deleteBook(Long bookId) {
		// TODO Auto-generated method stub
		
	}

	

	@Override
	public List<BookDto> getBooksByCategory(Long categoryId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BookDto updateStock(Long bookId, int stock) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BookDto uploadImage(Long bookId, MultipartFile file) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void bulkImportBooks(MultipartFile file) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<BookDto> getBooksBySeller(Long sellerId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BookDto featureBook(Long bookId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<BookDto> searchBooks(String keyword, Double minPrice, Double maxPrice) {
		 List<BookDto>  bookDtos=  bookrepository.searchBooks(keyword,minPrice ,maxPrice);
		return bookDtos;
	}

}
