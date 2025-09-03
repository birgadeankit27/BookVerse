package com.bookverser.BookVerse.service;
import com.bookverser.BookVerse.dto.BookDto;
import com.bookverser.BookVerse.dto.BookRequestDto;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface BookService {

    // 1. Add Book
    BookDto addBook(BookRequestDto request);

    // 2. Get All Books with pagination, sorting, filtering
    Page<BookDto> getAllBooks(Pageable pageable, String category, String author, Double minPrice, Double maxPrice);

    // 3. Get Book by ID
    BookDto getBookById(Long bookId);

    // 4. Update Book
    BookDto updateBook(Long bookId, BookRequestDto request);

    // 5. Delete Book (Soft Delete)
    void deleteBook(Long bookId);

    // 6. Search Books
    List<BookDto> searchBooks(String keyword, Double minPrice, Double maxPrice);

    // 7. Get Books by Category
    List<BookDto> getBooksByCategory(Long categoryId);

    // 8. Update Stock with Optimistic Locking
    BookDto updateStock(Long bookId, int stock);

    // 9. Upload Book Image
    BookDto uploadImage(Long bookId, MultipartFile file) throws IOException;

    // 10. Bulk Import Books
    void bulkImportBooks(MultipartFile file) throws IOException;

    // 11. Get Seller Books
    List<BookDto> getBooksBySeller(Long sellerId);

    // 12. Feature Book
    BookDto featureBook(Long bookId);
}
