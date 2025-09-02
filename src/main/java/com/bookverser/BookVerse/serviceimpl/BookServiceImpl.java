package com.bookverser.BookVerse.serviceimpl;

import com.bookverser.BookVerse.dto.BookRequest;
import com.bookverser.BookVerse.dto.BookDto;
import com.bookverser.BookVerse.entity.Book;
import com.bookverser.BookVerse.entity.User;
import com.bookverser.BookVerse.exception.DuplicateIsbnException;
import com.bookverser.BookVerse.entity.Category;
import com.bookverser.BookVerse.repository.BookRepository;
import com.bookverser.BookVerse.repository.UserRepository;
import com.bookverser.BookVerse.repository.CategoryRepository;
import com.bookverser.BookVerse.service.BookService;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public BookDto addBook(BookRequest request) {
        String uuid = UUID.randomUUID().toString();

        if (bookRepository.existsByIsbn(request.getIsbn())) {
            throw new DuplicateIsbnException("A book with ISBN " + request.getIsbn() + " already exists");
        }
        User seller = userRepository.findById(request.getSellerId())
                .orElseThrow(() -> new RuntimeException("Seller not found"));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        Book book = new Book();
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setDescription(request.getDescription());
        book.setIsbn(request.getIsbn());
        book.setPrice(request.getPrice());
        book.setStock(request.getStock());
        book.setCondition(request.getCondition());
        book.setImageUrl(request.getImageUrl());
        book.setSeller(seller);
        book.setCategory(category);
        book.setUuid(uuid);

        Book saved = bookRepository.save(book);

        return new BookDto(
                saved.getId(),
                saved.getTitle(),
                saved.getAuthor(),
                saved.getDescription(),
                saved.getUuid(),
                saved.getIsbn(),
                saved.getPrice(),
                saved.getStock(),
                saved.getCondition(),
                saved.getImageUrl(),
                saved.getStatus().name(),
                saved.getSeller().getId(),
                saved.getSeller().getName(),
                saved.getCategory().getId(),
                saved.getCategory().getName(),
                saved.getCreatedAt(),
                saved.getUpdatedAt()
        );
    }

    @Override
    public Page<BookDto> getAllBooks(Pageable pageable, String category, String author, Double minPrice, Double maxPrice) {
        // TODO: Implement filtering + pagination logic
        return Page.empty();
    }

    @Override
    public BookDto getBookById(Long bookId) {
        // TODO: Implement find by ID logic
        return null;
    }

    @Override
    public BookDto updateBook(Long bookId, BookRequest request) {
        // TODO: Implement update logic
        return null;
    }

    @Override
    public void deleteBook(Long bookId) {
        // TODO: Implement delete logic
    }

    @Override
    public List<BookDto> searchBooks(String keyword, Double minPrice, Double maxPrice) {
        // TODO: Implement search logic
        return List.of();
    }

    @Override
    public List<BookDto> getBooksByCategory(Long categoryId) {
        // TODO: Implement fetch by category
        return List.of();
    }

    @Override
    public BookDto updateStock(Long bookId, int stock) {
        // TODO: Implement stock update
        return null;
    }

    @Override
    public BookDto uploadImage(Long bookId, MultipartFile file) throws IOException {
        // TODO: Implement image upload logic
        return null;
    }

    @Override
    public void bulkImportBooks(MultipartFile file) throws IOException {
        // TODO: Implement bulk import
    }

    @Override
    public List<BookDto> getBooksBySeller(Long sellerId) {
        // TODO: Implement fetch by seller
        return List.of();
    }

    @Override
    public BookDto featureBook(Long bookId) {
        // TODO: Implement feature toggle
        return null;
    }
}
