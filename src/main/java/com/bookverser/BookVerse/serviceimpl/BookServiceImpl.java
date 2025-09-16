package com.bookverser.BookVerse.serviceimpl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

import com.bookverser.BookVerse.dto.BookDto;
import com.bookverser.BookVerse.dto.BulkImportBookRequestDTO;
import com.bookverser.BookVerse.dto.CreateBookRequestDTO;
import com.bookverser.BookVerse.dto.SearchBooksRequestDTO;
import com.bookverser.BookVerse.dto.UpdateBookRequestDTO;
import com.bookverser.BookVerse.dto.UpdateStockRequestDTO;
import com.bookverser.BookVerse.entity.Book;
import com.bookverser.BookVerse.entity.Category;
import com.bookverser.BookVerse.entity.User;
import com.bookverser.BookVerse.exception.CategoryNotFoundException;
import com.bookverser.BookVerse.exception.DuplicateIsbnException;
import com.bookverser.BookVerse.exception.InvalidPriceRangeException;
import com.bookverser.BookVerse.exception.InvalidSortParameterException;
import com.bookverser.BookVerse.exception.ResourceNotFoundException;

import com.bookverser.BookVerse.exception.UnauthorizedException;

import com.bookverser.BookVerse.repository.BookRepository;
import com.bookverser.BookVerse.repository.CategoryRepository;
import com.bookverser.BookVerse.repository.UserRepository;
import com.bookverser.BookVerse.security.CustomUserDetails;
import com.bookverser.BookVerse.service.BookService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

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

	// Inject upload directory from application.properties
	@Value("${file.upload-dir}")
	private String uploadDir;

	@Override
	@Transactional
	public BookDto addBook(CreateBookRequestDTO request) {
//		if (bookRepository.existsByIsbn(request.getIsbn())) {
//			throw new DuplicateIsbnException("ISBN already exists: " + request.getIsbn());
//		}
//
//		// Get authenticated seller
//		User seller = getAuthenticatedSeller();
//
//		// Fetch category
//		Category category = categoryRepository.findById(request.getCategoryId()).orElseThrow(
//				() -> new CategoryNotFoundException("Category not found with id: " + request.getCategoryId()));
//
//		// Map DTO -> Entity
//		Book book = new Book();
//		book.setTitle(request.getTitle());
//		book.setAuthor(request.getAuthor());
//		book.setDescription(request.getDescription());
//		book.setPrice(request.getPrice());
//		book.setIsbn(request.getIsbn());
//		book.setStock(request.getStock());
//		book.setCondition(request.getCondition());
//		book.setImageUrl(request.getImageUrl());
//		book.setCategory(category);
//		book.setSeller(seller);
//		book.setStatus("AVAILABLE");
//		book.setFeatured(false);
//		book.setActive(true);
//
//		// Save book
//		Book savedBook = bookRepository.save(book);
//
//		// Map Entity -> DTO
//		BookDto bookDto = new BookDto();
//		bookDto.setId(savedBook.getId());
//		bookDto.setTitle(savedBook.getTitle());
//		bookDto.setAuthor(savedBook.getAuthor());
//		bookDto.setDescription(savedBook.getDescription());
//		bookDto.setPrice(savedBook.getPrice());
//		bookDto.setIsbn(savedBook.getIsbn());
//		bookDto.setStock(savedBook.getStock());
//		bookDto.setCondition(savedBook.getCondition());
//		bookDto.setImageUrl(savedBook.getImageUrl());
//		bookDto.setCategoryId(savedBook.getCategory().getId());
//		bookDto.setSellerId(savedBook.getSeller().getId());
//		bookDto.setStatus(savedBook.getStatus());
//		bookDto.setFeatured(savedBook.isFeatured());
//
//		return bookDto;
		 // ✅ Check for duplicate ISBN
        if (bookRepository.existsByIsbn(request.getIsbn())) {
            throw new DuplicateIsbnException("ISBN already exists: " + request.getIsbn());
        }

        // ✅ Get authenticated seller
        User seller = getAuthenticatedSeller();

        // ✅ Fetch category
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new CategoryNotFoundException(
                        "Category not found with id: " + request.getCategoryId()));

        // ✅ Map DTO → Entity
        Book book = modelMapper.map(request, Book.class);
        book.setCategory(category);
        book.setSeller(seller);
        book.setStatus("AVAILABLE");
        book.setFeatured(false);
        book.setActive(true);

        // ✅ Save book
        Book savedBook = bookRepository.save(book);

        // ✅ Map Entity → DTO
        BookDto bookDto = modelMapper.map(savedBook, BookDto.class);
        bookDto.setCategoryId(savedBook.getCategory().getId());
        bookDto.setSellerId(savedBook.getSeller().getId());

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
		boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

		if (!isAdmin && !userDetails.getId().equals(sellerId)) {
			throw new UnauthorizedException("You are not allowed to access books of another seller");
		}

		List<Book> books = bookRepository.findBySeller_Id(sellerId);

		if (books.isEmpty()) {
			throw new ResourceNotFoundException("No books found for seller with ID: " + sellerId);
		}

		return books.stream().map(book -> modelMapper.map(book, BookDto.class)).collect(Collectors.toList());
	}

	@Override
	public Page<BookDto> getAllBooks(Pageable pageable, String category, String author, Double minPrice,
			Double maxPrice) {
		Page<Book> books = bookRepository.findAll(pageable); // You can later add filters

		// Convert Page<Book> → Page<BookDto>
		return books.map(book -> modelMapper.map(book, BookDto.class));

	}

	@Override
	public BookDto getBookById(Long bookId) {
		Book book = bookRepository.findById(bookId)
				.orElseThrow(() -> new ResourceNotFoundException("Book not found with id " + bookId));
		return new BookDto(book.getId(), book.getTitle(), book.getAuthor(), book.getDescription(), book.getPrice(),
				book.getIsbn(), book.getStock(), book.getCondition(), book.getImageUrl(), book.getCategory().getId());
	}

	@Override
	public BookDto updateBook(Long bookId, UpdateBookRequestDTO request) {

		// TODO implement update logic

		return null;
	}

	@Override
	public void deleteBook(Long bookId) {

	}

	@Override
	public List<BookDto> getBooksByCategory(String categoryName) {
		List<Book> books = bookRepository.findByCategory_Name(categoryName);
		if (books.isEmpty()) {
			return List.of();
		}
		return books.stream().map(book -> modelMapper.map(book, BookDto.class)).toList();
	}

	@Override
	public List<BookDto> getBooksByCategory(Long categoryId) {
		return null;
	}

	@Override
	public BookDto updateStock(Long bookId, UpdateStockRequestDTO request) {
		Book book = bookRepository.findById(bookId)
				.orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + bookId));
		book.setStock(request.getStock());
		bookRepository.save(book);
		return new BookDto(book.getId(), book.getTitle(), book.getAuthor(), book.getDescription(), book.getPrice(),
				book.getIsbn(), book.getStock(), book.getCondition(), book.getImageUrl(), book.getCategory().getId());
	}

	@Override
	public BookDto uploadImage(Long bookId, MultipartFile file) throws IOException {
		Book book = bookRepository.findById(bookId)
				.orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + bookId));

		if (file.isEmpty()) {
			throw new IllegalArgumentException("Uploaded file is empty");
		}

		File dir = new File(uploadDir);
		if (!dir.exists()) {
			boolean created = dir.mkdirs();
			if (!created) {
				throw new IOException("Failed to create upload directory: " + uploadDir);
			}
		}

		String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
		Path filePath = Paths.get(uploadDir, fileName);

		try {
			Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			throw new IOException("Failed to save file: " + fileName, e);
		}

		String imageUrl = "/uploads/" + fileName;
		book.setImageUrl(imageUrl);
		bookRepository.save(book);

		return modelMapper.map(book, BookDto.class);
	}

	@Override
	@Transactional
	public void bulkImportBooks(MultipartFile file) throws IOException {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !auth.isAuthenticated()) {
			throw new UnauthorizedException("User not authenticated");
		}

		// ✅ Check if admin
		boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
		if (!isAdmin) {
			throw new UnauthorizedException("Only admins can bulk import books");
		}

		String filename = file.getOriginalFilename();
		if (filename == null || (!filename.endsWith(".csv") && !filename.endsWith(".json"))) {
			throw new IllegalArgumentException("Invalid file format. Only CSV and JSON are supported.");
		}

		List<Book> books;
		if (filename.endsWith(".csv")) {
			books = parseCsv(file);
		} else {
			books = parseJson(file);
		}

		// ✅ Check duplicates inside file
		var seenIsbns = new java.util.HashSet<String>();
		for (Book b : books) {
			if (!seenIsbns.add(b.getIsbn())) {
				throw new DuplicateIsbnException("Duplicate ISBN in file: " + b.getIsbn());
			}
		}

		// ✅ Check duplicates against DB
		for (Book b : books) {
			if (bookRepository.existsByIsbn(b.getIsbn())) {
				throw new DuplicateIsbnException("Duplicate ISBN already exists in DB: " + b.getIsbn());
			}
		}

		// ✅ Save all books
		bookRepository.saveAll(books);
	}

	private List<Book> parseCsv(MultipartFile file) throws IOException {
		List<Book> books = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
			String header = br.readLine(); // Skip header
			String line;

			while ((line = br.readLine()) != null) {
				String[] parts = line.split(",");

				if (parts.length < 10) {
					throw new IllegalArgumentException("Invalid CSV format. Expected 10 fields, got: " + parts.length);
				}

				// Map CSV → DTO
				BulkImportBookRequestDTO dto = new BulkImportBookRequestDTO();
				dto.setIsbn(parts[0].trim());
				dto.setTitle(parts[1].trim());
				dto.setAuthor(parts[2].trim());
				dto.setPrice(Double.parseDouble(parts[3].trim()));
				dto.setDescription(parts[4].trim());
				dto.setImageUrl(parts[5].trim());
				dto.setCategoryId(Long.parseLong(parts[6].trim()));
				Long sellerId = Long.parseLong(parts[7].trim()); // ✅ sellerId from file
				dto.setStock(Integer.parseInt(parts[8].trim()));
				dto.setCondition(parts[9].trim());

				if (!dto.getCondition().matches("NEW|GOOD|OLD")) {
					throw new IllegalArgumentException("Invalid condition value: " + dto.getCondition());
				}

				// Fetch related entities
				Category category = categoryRepository.findById(dto.getCategoryId())
						.orElseThrow(() -> new CategoryNotFoundException("Category not found: " + dto.getCategoryId()));
				User seller = userRepository.findById(sellerId)
						.orElseThrow(() -> new ResourceNotFoundException("Seller not found: " + sellerId));

				// Map DTO → Entity
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
				new TypeReference<List<Map<String, Object>>>() {
				});

		List<Book> books = new ArrayList<>();
		for (Map<String, Object> map : rawList) {
			String isbn = (String) map.get("isbn");
			String title = (String) map.get("title");
			String author = (String) map.get("author");
			Double price = Double.valueOf(map.get("price").toString());
			String description = (String) map.getOrDefault("description", "");
			String imageUrl = (String) map.getOrDefault("imageUrl", "");
			Long categoryId = Long.valueOf(map.get("categoryId").toString());
			Long sellerId = Long.valueOf(map.get("sellerId").toString()); // ✅ sellerId from JSON
			Integer stock = Integer.valueOf(map.get("stock").toString());
			String condition = (String) map.get("condition");

			if (!condition.matches("NEW|GOOD|OLD")) {
				throw new IllegalArgumentException("Invalid condition value: " + condition);
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
	public List<BookDto> searchBooks(SearchBooksRequestDTO request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BookDto featureBook(Long bookId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<BookDto> searchBooks(String title, String author, String isbn) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<BookDto> filterBooks(String category, Double minPrice, Double maxPrice, String location) {
		if (minPrice != null && maxPrice != null && minPrice > maxPrice) {
			throw new InvalidPriceRangeException("Min price must be smaller than max price");
		}

		List<Book> books;
		if (category == null && minPrice == null && maxPrice == null && location == null) {
			books = bookRepository.findAll();
		} else {
			books = bookRepository.findAllFilter(category, minPrice, maxPrice, location);
		}

		return books.stream().map(book -> modelMapper.map(book, BookDto.class)).collect(Collectors.toList());
	}

	@Override
	public List<BookDto> sortBooks(String sortBy) {
		List<Book> books;

		String sort = sortBy.toLowerCase();

		if (!sort.equals("latest") && !sort.equals("priceasc") && !sort.equals("pricedesc") && !sort.equals("rating")
				&& sort != null) {
			throw new InvalidSortParameterException("Give the proper sorting string");
		} else if (sortBy == null || sortBy.equalsIgnoreCase("latest")) {
			books = bookRepository.findAllByLatest();
		} else if (sortBy.equalsIgnoreCase("priceAsc")) {
			books = bookRepository.findAllByPriceAsc();
		} else if (sortBy.equalsIgnoreCase("priceDesc")) {
			books = bookRepository.findAllByPriceDesc();
		} else if (sortBy.equalsIgnoreCase("rating")) {
			books = bookRepository.findAllByRating();
		} else {
			books = bookRepository.findAll();
		}

		return books.stream().map(book -> modelMapper.map(book, BookDto.class)).collect(Collectors.toList());
	}

}
