



package com.bookverser.BookVerse.service;

import com.bookverser.BookVerse.dto.CreateBookRequestDTO;
import com.bookverser.BookVerse.dto.UpdateBookRequestDTO;
import com.bookverser.BookVerse.dto.UpdateStockRequestDTO;
import com.bookverser.BookVerse.dto.BookDto;

import com.bookverser.BookVerse.dto.SearchBooksRequestDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * BookService Interface
 * Defines methods for managing books in the bookstore application.
 * Supports operations for adding, retrieving, updating, deleting, searching,
 * and managing book-related data for sellers and admins.
 */
public interface BookService {

    /**
     * Add a new book (BOOK:SELLER:ADD-BOOK).
     * Only authenticated sellers/admins can add books.
     * @param request DTO containing book details.
     * @return BookDTO with created book details.
     * @throws MethodArgumentNotValidException if mandatory fields are invalid (400).
     * @throws DuplicateIsbnException if ISBN already exists (409).
     * @throws UnauthorizedException if user is not authenticated (401).
     */
    BookDto addBook(CreateBookRequestDTO request);

    /**
     * Retrieve a paginated list of available books (BOOK:PUBLIC:GET-ALL).
     * Supports pagination, sorting, and filtering by category, author, price range.
     * @param pageable Pagination and sorting parameters.
     * @param category Filter by category ID (optional).
     * @param author Filter by author name (optional).
     * @param minPrice Minimum price filter (optional).
     * @param maxPrice Maximum price filter (optional).
     * @return Page<BookDTO> of in-stock books.
     */
    Page<BookDto> getAllBooks(Pageable pageable, String category, String author, Double minPrice, Double maxPrice);

    /**
     * Get a book by its ID (BOOK:PUBLIC:GET-BY-ID).
     * Publicly accessible.
     * @param bookId ID of the book.
     * @return BookDTO with book details.
     * @throws ResourceNotFoundException if book not found (404).
     */
    BookDto getBookById(Long bookId);

    /**
     * Update a book's details (BOOK:SELLER:UPDATE-BOOK).
     * Only book owner (seller) or admin can update. ISBN cannot be updated.
     * @param bookId ID of the book to update.
     * @param request DTO with updated book details.
     * @return Updated BookDTO.
     * @throws MethodArgumentNotValidException if invalid fields (400).
     * @throws ResourceNotFoundException if book not found (404).
     * @throws UnauthorizedException if user is not authorized (403).
     */
    BookDto updateBook(Long bookId, UpdateBookRequestDTO request);

    /**
     * Soft delete a book (BOOK:SELLER:DELETE-BOOK).
     * Only seller or admin can delete.
     * @param bookId ID of the book to delete.
     * @throws ResourceNotFoundException if book not found (404).
     * @throws UnauthorizedException if user is not authorized (403).
     */
    void deleteBook(Long bookId);

    /**
     * Search books by keyword and price range (BOOK:PUBLIC:SEARCH).
     * Case-insensitive keyword search with optional price filters.
     * @param request DTO with search parameters (keyword, minPrice, maxPrice).
     * @return List<BookDTO> of matching books.
     */
    List<BookDto> searchBooks(SearchBooksRequestDTO request);

    /**
     * Get books by category (BOOK:PUBLIC:GET-BY-CATEGORY).
     * @param categoryId ID of the category.
     * @return List<BookDTO> of books in the category.
     * @throws ResourceNotFoundException if category not found (404).
     */
    List<BookDto> getBooksByCategory(String categoryName);

    /**
     * Update book stock with optimistic locking (BOOK:SELLER:UPDATE-STOCK).
     * Logs stock changes and handles concurrent updates.
     * @param bookId ID of the book.
     * @param request DTO with new stock value.
     * @return Updated BookDTO.
     * @throws ResourceNotFoundException if book not found (404).
     * @throws MethodArgumentNotValidException if stock is invalid (400).
     * @throws Exception for concurrency issues or other errors (500).
     */
    BookDto updateStock(Long bookId, UpdateStockRequestDTO request);

    /**
     * Upload a book image (BOOK:SELLER:UPLOAD-IMAGE).
     * Validates file format and book existence.
     * @param bookId ID of the book.
     * @param file Image file to upload.
     * @return Updated BookDTO with new image URL.
     * @throws ResourceNotFoundException if book not found (404).
     * @throws InvalidFileFormatException if file format is invalid (400).
     * @throws IOException for file processing errors.
     */
    BookDto uploadImage(Long bookId, MultipartFile file) throws IOException;

    /**
     * Bulk import books from a file (BOOK:ADMIN:BULK-IMPORT).
     * Admin-only operation. Validates file format and content, checks for duplicate ISBNs.
     * @param file File containing book data (e.g., CSV/JSON).
     * @throws UnauthorizedException if user is not admin (401).
     * @throws DuplicateIsbnException if duplicate ISBNs found (409).
     * @throws MethodArgumentNotValidException if file content is invalid (400).
     * @throws IOException for file processing errors.
     */
    void bulkImportBooks(MultipartFile file) throws IOException;

    /**
     * Get books by seller (BOOK:SELLER:GET-MY-BOOKS).
     * Seller can only access their own books.
     * @param sellerId ID of the seller.
     * @return List<BookDTO> of seller's books.
     * @throws UnauthorizedException if user is not authorized (403).
     */
    List<BookDto> getBooksBySeller(Long sellerId);

    /**
     * Feature a book (BOOK:ADMIN:FEATURE-BOOK).
     * Admin-only operation to mark a book as featured.
     * @param bookId ID of the book to feature.
     * @return Updated BookDTO with isFeatured set to true.
     * @throws UnauthorizedException if user is not admin (403).
     * @throws ResourceNotFoundException if book not found (404).
     */
    BookDto featureBook(Long bookId);
    
    
    
    List<BookDto> searchBooks(String title, String author, String isbn);

    /**
     * Filter books by category, price range, and location
     */
    List<BookDto> filterBooks(String category, Double minPrice, Double maxPrice, String location);

    /**
     * Sort books by latest, price, or rating
     */
    List<BookDto> sortBooks(String sortBy);
    
    
 
}
