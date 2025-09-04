package com.bookverser.BookVerse.serviceimpl;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.bookverser.BookVerse.dto.BookDto;
import com.bookverser.BookVerse.dto.BookRequestDto;
import com.bookverser.BookVerse.entity.Book;
import com.bookverser.BookVerse.repository.BookRepository;
import com.bookverser.BookVerse.service.BookService;


@Service
public class BookServiceImpl implements BookService {
	
	@Autowired
	private BookRepository bookRepository;

	@Override
	public BookDto addBook(BookRequestDto request) {
		
		return null;
	}

	@Override
	public Page<BookDto> getAllBooks(Pageable pageable, String category, String author, Double minPrice, Double maxPrice) {
	    System.out.println("hello");
		Page<Book> books = bookRepository.findAll(pageable); // for now, just fetch all

	    return books.map(BookDto::fromEntity); // convert entity → dto
	}


	@Override
	public BookDto getBookById(Long bookId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BookDto updateBook(Long bookId, BookRequestDto request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteBook(Long bookId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<BookDto> searchBooks(String keyword, Double minPrice, Double maxPrice) {
		// TODO Auto-generated method stub
		return null;
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

	// Directory path from application.properties
    @Value("${book.upload.dir}")
    private String uploadDir;

	@Override
	public BookDto uploadImage(Long bookId, MultipartFile file) throws IOException {
	    // 1. Fetch book
	    Book book = bookRepository.findById(bookId)
	            .orElseThrow(() -> new RuntimeException("Book not found with id: " + bookId));

	    // 2. Ensure upload directory exists
	    File dir = new File(uploadDir);
	    if (!dir.exists()) {
	        dir.mkdirs();
	    }

	    // 3. Generate unique filename
	    String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
	    Path filePath = Paths.get(uploadDir, fileName);

	    // 4. Save file to server
	    Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

	    // 5. Update book imageUrl (mapped to /uploads/**)
	    book.setImageUrl("/uploads/" + fileName);
	    bookRepository.save(book);

	    // 6. Convert Book -> BookDto (assuming you have a mapper)
	    return BookDto.fromEntity(book);
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

}
