package com.bookverser.BookVerse.serviceimpl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.bookverser.BookVerse.dto.*;
import com.bookverser.BookVerse.entity.*;
import com.bookverser.BookVerse.exception.*;
import com.bookverser.BookVerse.exception.ResourceNotFoundException;
import com.bookverser.BookVerse.repository.*;
import com.bookverser.BookVerse.security.CustomUserDetails;
import com.bookverser.BookVerse.service.BookService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;

@Service
public class BookServiceImpl implements BookService {

    @Autowired private BookRepository bookRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ModelMapper modelMapper;

    @Value("${file.upload-dir}")
    private String uploadDir;

    private BookDto mapToDto(Book book) {
        return modelMapper.map(book, BookDto.class);
    }

    // ------------------- Add Book -------------------
    @Override
    @Transactional
    public BookDto addBook(CreateBookRequestDTO request) {
        if (bookRepository.existsByIsbn(request.getIsbn())) {
            throw new DuplicateIsbnException("ISBN already exists: " + request.getIsbn());
        }

        User seller = getAuthenticatedSeller();

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with id: " + request.getCategoryId()));

        Book book = modelMapper.map(request, Book.class);
        book.setId(System.currentTimeMillis() * 1000);
        book.setCategory(category);
        book.setSeller(seller);
        book.setStatus("AVAILABLE");
        book.setFeatured(false);
        book.setActive(true);

        Book savedBook = bookRepository.save(book);

        BookDto dto = modelMapper.map(savedBook, BookDto.class);
        dto.setCategoryId(savedBook.getCategory().getId());
        dto.setSellerId(savedBook.getSeller().getId());
        return dto;
    }

    private User getAuthenticatedSeller() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new UnauthorizedException("User not authenticated");
        }
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        return userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new UnauthorizedException("Authenticated user not found"));
    }

    // ------------------- Get Books By Seller -------------------
    @Override
    public List<BookDto> getBooksBySeller(Long sellerId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new UnauthorizedException("User not authenticated");
        }

        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !userDetails.getId().equals(sellerId)) {
            throw new UnauthorizedException("You are not allowed to access books of another seller");
        }

        List<Book> books = bookRepository.findBySeller_Id(sellerId);
        if (books.isEmpty()) {
            throw new ResourceNotFoundException("No books found for seller with ID: " + sellerId);
        }

        return books.stream().map(this::mapToDto).toList();
    }

    // ------------------- Get All Books (with paging) -------------------
    @Override
    public Page<BookDto> getAllBooks(Pageable pageable, String category, String author,
                                     BigDecimal minPrice, BigDecimal maxPrice) {
        Page<Book> books = bookRepository.findAll(pageable); // filters can be added later
        return books.map(book -> modelMapper.map(book, BookDto.class));
    }

    // ------------------- Update Book -------------------
    @Override
    @Transactional
    public BookDto updateBook(Long bookId, UpdateBookRequestDTO request) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + bookId));

        if (request.getTitle() != null) book.setTitle(request.getTitle().trim());
        if (request.getAuthor() != null) book.setAuthor(request.getAuthor().trim());
        if (request.getDescription() != null) book.setDescription(request.getDescription());
        if (request.getPrice() != null) book.setPrice(request.getPrice());
        if (request.getStock() != null) book.setStock(request.getStock());
        if (request.getCondition() != null) book.setCondition(request.getCondition());
        if (request.getImageUrl() != null) book.setImageUrl(request.getImageUrl());

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new CategoryNotFoundException("Category not found with id: " + request.getCategoryId()));
            book.setCategory(category);
        }

        Book updated = bookRepository.save(book);
        BookDto dto = modelMapper.map(updated, BookDto.class);
        dto.setCategoryId(updated.getCategory().getId());
        dto.setSellerId(updated.getSeller().getId());
        return dto;
    }

    // ------------------- Delete Book -------------------
    @Override
    @Transactional
    public void deleteBook(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + bookId));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new UnauthorizedException("User not authenticated");
        }
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        User authUser = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new UnauthorizedException("Authenticated user not found"));

        boolean isOwner = book.getSeller() != null && book.getSeller().getId().equals(authUser.getId());
        boolean isAdmin = userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isOwner && !isAdmin) {
            throw new UnauthorizedException("Only book owner or admin can delete this book");
        }

        book.setActive(false);
        bookRepository.save(book);
    }

    // ------------------- Stock Update -------------------
    @Override
    public BookDto updateStock(Long bookId, UpdateStockRequestDTO request) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + bookId));

        book.setStock(request.getStock());
        bookRepository.save(book);

        return mapToDto(book);
    }

    // ------------------- Get Book By Id -------------------
    @Override
    public BookDto getBookById(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id " + bookId));
        return mapToDto(book);
    }

    // ------------------- Get Books By Category -------------------
    @Override
    public List<BookDto> getBooksByCategory(String categoryName) {
        List<Book> books = bookRepository.findByCategory_Name(categoryName);
        if (books.isEmpty()) {
            throw new ResourceNotFoundException("No books found for category: " + categoryName);
        }
        return books.stream().map(this::mapToDto).toList();
    }

    @Override
    public List<BookDto> getBooksByCategory(Long categoryId) {
        // TODO implement if needed
        return List.of();
    }

    // ------------------- Search Books -------------------
    @Override
    public List<BookDto> searchBooks(String title, String author, String categoryName, String isbn) {
        if ((title == null || title.isBlank()) &&
            (author == null || author.isBlank()) &&
            (categoryName == null || categoryName.isBlank()) &&
            (isbn == null || isbn.isBlank())) {
            throw new InvalidRequestException("At least one search parameter must be provided.");
        }

        List<Book> books = bookRepository.searchBooks(title, author, categoryName, isbn);
        if (books.isEmpty()) {
            throw new ResourceNotFoundException("No books found matching the given criteria.");
        }

        return books.stream().map(this::mapToDto).toList();
    }

    // ------------------- Filter Books -------------------
    @Override
    public List<BookDto> filterBooks(String category, BigDecimal minPrice, BigDecimal maxPrice, String location) {
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new InvalidPriceRangeException("Min price must be smaller than max price");
        }

        List<Book> books = (category == null && minPrice == null && maxPrice == null && location == null)
                ? bookRepository.findAll()
                : bookRepository.findAllFilter(category, minPrice, maxPrice, location);

        return books.stream().map(this::mapToDto).toList();
    }

    // ------------------- Sort Books -------------------
    @Override
    public List<BookDto> sortBooks(String sortBy) {
        List<Book> books;
        if (sortBy == null || sortBy.equalsIgnoreCase("latest")) {
            books = bookRepository.findAllByLatest();
        } else if (sortBy.equalsIgnoreCase("priceAsc")) {
            books = bookRepository.findAllByPriceAsc();
        } else if (sortBy.equalsIgnoreCase("priceDesc")) {
            books = bookRepository.findAllByPriceDesc();
        } else if (sortBy.equalsIgnoreCase("rating")) {
            books = bookRepository.findAllByRating();
        } else {
            throw new InvalidSortParameterException("Invalid sort parameter: " + sortBy);
        }
        return books.stream().map(this::mapToDto).toList();
    }

    // ------------------- Featured Books -------------------
    @Override
    public BookDto markBookAsFeatured(Long bookId, boolean isFeatured) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with ID: " + bookId));
        book.setFeatured(isFeatured);
        return mapToDto(bookRepository.save(book));
    }

    @Override
    public List<BookDto> getFeaturedBooks() {
        return bookRepository.findByFeaturedTrue().stream()
                .map(this::mapToDto)
                .toList();
    }

    // ------------------- Bulk Import -------------------
    @Override
    @Transactional
    public void bulkImportBooks(MultipartFile file) throws IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() ||
                auth.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            throw new UnauthorizedException("Only admins can bulk import books");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.endsWith(".csv") && !filename.endsWith(".json"))) {
            throw new IllegalArgumentException("Invalid file format. Only CSV and JSON are supported.");
        }

        List<Book> books = filename.endsWith(".csv") ? parseCsv(file) : parseJson(file);

        Set<String> seenIsbns = new HashSet<>();
        for (Book b : books) {
            if (!seenIsbns.add(b.getIsbn())) {
                throw new DuplicateIsbnException("Duplicate ISBN in file: " + b.getIsbn());
            }
            if (bookRepository.existsByIsbn(b.getIsbn())) {
                throw new DuplicateIsbnException("ISBN already exists in DB: " + b.getIsbn());
            }
        }

        bookRepository.saveAll(books);
    }

    private List<Book> parseCsv(MultipartFile file) throws IOException {
        List<Book> books = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            br.readLine(); // skip header
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length < 10) {
                    throw new IllegalArgumentException("Invalid CSV format, expected 10 fields");
                }
                BulkImportBookRequestDTO dto = new BulkImportBookRequestDTO();
                dto.setIsbn(parts[0].trim());
                dto.setTitle(parts[1].trim());
                dto.setAuthor(parts[2].trim());
                dto.setPrice(new BigDecimal(parts[3].trim()));
                dto.setDescription(parts[4].trim());
                dto.setImageUrl(parts[5].trim());
                dto.setCategoryId(Long.parseLong(parts[6].trim()));
                Long sellerId = Long.parseLong(parts[7].trim());
                dto.setStock(Integer.parseInt(parts[8].trim()));
                dto.setCondition(parts[9].trim());

                if (!dto.getCondition().matches("NEW|GOOD|OLD")) {
                    throw new IllegalArgumentException("Invalid condition: " + dto.getCondition());
                }

                Category category = categoryRepository.findById(dto.getCategoryId())
                        .orElseThrow(() -> new CategoryNotFoundException("Category not found: " + dto.getCategoryId()));
                User seller = userRepository.findById(sellerId)
                        .orElseThrow(() -> new ResourceNotFoundException("Seller not found: " + sellerId));

                Book book = new Book();
                book.setIsbn(dto.getIsbn());
                book.setTitle(dto.getTitle());
                book.setAuthor(dto.getAuthor());
                book.setPrice(dto.getPrice());
                book.setDescription(dto.getDescription());
                book.setImageUrl(dto.getImageUrl());
                book.setCategory(category);
                book.setSeller(seller);
                book.setStock(dto.getStock());
                book.setCondition(dto.getCondition());
                book.setStatus("AVAILABLE");
                book.setFeatured(false);
                book.setActive(true);

                books.add(book);
            }
        }
        return books;
    }

    private List<Book> parseJson(MultipartFile file) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        List<Map<String, Object>> rawList = mapper.readValue(file.getInputStream(),
                new TypeReference<List<Map<String, Object>>>() {});
        List<Book> books = new ArrayList<>();
        for (Map<String, Object> map : rawList) {
            String isbn = (String) map.get("isbn");
            String title = (String) map.get("title");
            String author = (String) map.get("author");
            BigDecimal price = new BigDecimal(map.get("price").toString());
            String description = (String) map.getOrDefault("description", "");
            String imageUrl = (String) map.getOrDefault("imageUrl", "");
            Long categoryId = Long.valueOf(map.get("categoryId").toString());
            Long sellerId = Long.valueOf(map.get("sellerId").toString());
            Integer stock = Integer.valueOf(map.get("stock").toString());
            String condition = (String) map.get("condition");

            if (!condition.matches("NEW|GOOD|OLD")) {
                throw new IllegalArgumentException("Invalid condition: " + condition);
            }

            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new CategoryNotFoundException("Category not found: " + categoryId));
            User seller = userRepository.findById(sellerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Seller not found: " + sellerId));

            Book book = new Book();
            book.setIsbn(isbn);
            book.setTitle(title);
            book.setAuthor(author);
            book.setPrice(price);
            book.setDescription(description);
            book.setImageUrl(imageUrl);
            book.setCategory(category);
            book.setSeller(seller);
            book.setStock(stock);
            book.setCondition(condition);
            book.setStatus("AVAILABLE");
            book.setFeatured(false);
            book.setActive(true);

            books.add(book);
        }
        return books;
    }

	@Override
	public BookDto uploadImage(Long bookId, MultipartFile file) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}
}
