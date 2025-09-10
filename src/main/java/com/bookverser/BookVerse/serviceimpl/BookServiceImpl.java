package com.bookverser.BookVerse.serviceimpl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.bookverser.BookVerse.dto.BookDto;
import com.bookverser.BookVerse.dto.CreateBookRequestDTO;
import com.bookverser.BookVerse.dto.SearchBooksRequestDTO;
import com.bookverser.BookVerse.dto.UpdateBookRequestDTO;
import com.bookverser.BookVerse.dto.UpdateStockRequestDTO;
import com.bookverser.BookVerse.entity.Book;
import com.bookverser.BookVerse.entity.Category;
import com.bookverser.BookVerse.entity.User;
import com.bookverser.BookVerse.exception.CategoryNotFoundException;
import com.bookverser.BookVerse.exception.DuplicateIsbnException;
import com.bookverser.BookVerse.repository.BookRepository;
import com.bookverser.BookVerse.repository.CategoryRepository;
import com.bookverser.BookVerse.repository.UserRepository;
import com.bookverser.BookVerse.security.CustomUserDetails;
import com.bookverser.BookVerse.service.BookService;

import jakarta.transaction.Transactional;

@Service
public class BookServiceImpl implements BookService {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ModelMapper modelMapper;

   
    @Override
    @Transactional
    public BookDto addBook(CreateBookRequestDTO request) {
    	if (bookRepository.existsByIsbn(request.getIsbn())) {
            throw new DuplicateIsbnException("ISBN already exists: " + request.getIsbn());
        }

        // Get authenticated seller
        User seller = getAuthenticatedSeller();

        // Fetch category
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new CategoryNotFoundException(
                        "Category not found with id: " + request.getCategoryId()));

        // Map DTO -> Entity
        Book book = new Book();
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setDescription(request.getDescription());
        book.setPrice(request.getPrice());
        book.setIsbn(request.getIsbn());
        book.setStock(request.getStock());
        book.setCondition(request.getCondition());
        book.setImageUrl(request.getImageUrl());
        book.setCategory(category);
        book.setSeller(seller);
        book.setStatus("AVAILABLE");
        book.setFeatured(false);
        book.setActive(true);

        // Save book
        Book savedBook = bookRepository.save(book);

        // Map Entity -> DTO
        BookDto bookDto = new BookDto();
        bookDto.setId(savedBook.getId());
        bookDto.setTitle(savedBook.getTitle());
        bookDto.setAuthor(savedBook.getAuthor());
        bookDto.setDescription(savedBook.getDescription());
        bookDto.setPrice(savedBook.getPrice());
        bookDto.setIsbn(savedBook.getIsbn());
        bookDto.setStock(savedBook.getStock());
        bookDto.setCondition(savedBook.getCondition());
        bookDto.setImageUrl(savedBook.getImageUrl());
        bookDto.setCategoryId(savedBook.getCategory().getId());
        bookDto.setSellerId(savedBook.getSeller().getId());
        bookDto.setStatus(savedBook.getStatus());
        bookDto.setFeatured(savedBook.isFeatured());

        return bookDto;
    }

    // Helper method to get authenticated seller
    private User getAuthenticatedSeller() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();

        return userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
    }

    // ------------------- Other methods (stubs) -------------------

    @Override
    public Page<BookDto> getAllBooks(Pageable pageable, String category, String author, Double minPrice, Double maxPrice) {
        System.out.println("hello");

        Page<Book> books = bookRepository.findAll(pageable); // You can later add filters for category, author, etc.

        // Convert Page<Book> → Page<BookDto> using ModelMapper
        return books.map(book -> modelMapper.map(book, BookDto.class));
    }


    @Override
    public BookDto getBookById(Long bookId) {
        // TODO
        return null;
    }

    @Override
    public BookDto updateBook(Long bookId, UpdateBookRequestDTO request) {
        // TODO
        return null;
    }

    @Override
    public void deleteBook(Long bookId) {
        // TODO
    }

  
    @Override
    public List<BookDto> searchBooks(SearchBooksRequestDTO request) {
        List<Book> books = bookRepository.searchBooks(
                request.getKeyword(),
                request.getMinPrice(),
                request.getMaxPrice()
        );

        if (books.isEmpty()) {
            return List.of(); // Controller can return 204
        }

        return books.stream()
                .map(book -> {
                    BookDto dto = modelMapper.map(book, BookDto.class);
                    dto.setCategoryId(book.getCategory().getId());
                    dto.setSellerId(book.getSeller().getId());
                    return dto;
                })
                .collect(Collectors.toList());
    }



    @Override
    public List<BookDto> getBooksByCategory(Long categoryId) {
        // TODO
        return null;
    }

    @Override
    public BookDto updateStock(Long bookId, UpdateStockRequestDTO request) {
        // TODO
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

        // 6. Convert Book -> BookDto using ModelMapper
        return modelMapper.map(book, BookDto.class);
    }


    @Override
    public void bulkImportBooks(MultipartFile file) throws IOException {
        // TODO
    }

    @Override
    public List<BookDto> getBooksBySeller(Long sellerId) {
        // TODO
        return null;
    }

    @Override
    public BookDto featureBook(Long bookId) {
        // TODO
        return null;
    }
}
