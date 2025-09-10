package com.bookverser.BookVerse.serviceimpl;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
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
import com.bookverser.BookVerse.exception.ResourceNotFoundException;
import com.bookverser.BookVerse.exception.UnauthorizedException;
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

    // ------------------- Add Book -------------------
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

    // Helper: Get authenticated seller
    private User getAuthenticatedSeller() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();

        return userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
    }

    // ------------------- Get Books By Seller -------------------
    
    @Override
    public List<BookDto> getBooksBySeller(Long sellerId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new UnauthorizedException("User not authenticated");
        }

        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();

        // Check if user is the same seller OR is an admin
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !userDetails.getId().equals(sellerId)) {
            throw new UnauthorizedException("You are not allowed to access books of another seller");
        }

        List<Book> books = bookRepository.findBySeller_Id(sellerId);

        if (books.isEmpty()) {
            throw new ResourceNotFoundException("No books found for seller with ID: " + sellerId);
        }

        return books.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }


    private BookDto convertToDto(Book book) {
        BookDto dto = new BookDto();
        dto.setId(book.getId());
        dto.setIsbn(book.getIsbn());
        dto.setTitle(book.getTitle());
        dto.setAuthor(book.getAuthor());
        dto.setPrice(book.getPrice());
        dto.setDescription(book.getDescription());
        dto.setImageUrl(book.getImageUrl());
        dto.setCondition(book.getCondition());
        dto.setStatus(book.getStatus());
        dto.setFeatured(book.isFeatured());
        dto.setStock(book.getStock());
        dto.setCategoryId(book.getCategory().getId());
        dto.setSellerId(book.getSeller().getId());
        return dto;
    }

    // ------------------- Other Methods (Stubs) -------------------
    @Override
    public Page<BookDto> getAllBooks(Pageable pageable, Long category, String author, Double minPrice, Double maxPrice) {
        return null;
    }

    @Override
    public BookDto getBookById(Long bookId) {
        return null;
    }

    @Override
    public BookDto updateBook(Long bookId, UpdateBookRequestDTO request) {
        return null;
    }

    @Override
    public void deleteBook(Long bookId) {
    }

    @Override
    public List<BookDto> searchBooks(SearchBooksRequestDTO request) {
        List<Book> books = bookRepository.searchBooks(
                request.getKeyword(),
                request.getMinPrice(),
                request.getMaxPrice()
        );

        if (books.isEmpty()) {
            return List.of();
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
        return null;
    }

    @Override
    public BookDto updateStock(Long bookId, UpdateStockRequestDTO request) {
        return null;
    }

    @Override
    public BookDto uploadImage(Long bookId, MultipartFile file) throws IOException {
        return null;
    }

    @Override
    public void bulkImportBooks(MultipartFile file) throws IOException {
    }

    @Override
    public BookDto featureBook(Long bookId) {
        return null;
    }
}
