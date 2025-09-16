package com.bookverser.BookVerse.serviceimpl;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
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
import com.bookverser.BookVerse.repository.BookRepository;
import com.bookverser.BookVerse.repository.CategoryRepository;
import com.bookverser.BookVerse.repository.UserRepository;
import com.bookverser.BookVerse.security.CustomUserDetails;
import com.bookverser.BookVerse.service.BookService;
import com.bookverser.BookVerse.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import com.bookverser.BookVerse.exception.UnauthorizedException;
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
		Category category = categoryRepository.findById(request.getCategoryId()).orElseThrow(
				() -> new CategoryNotFoundException("Category not found with id: " + request.getCategoryId()));

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
	public Page<BookDto> getAllBooks(Pageable pageable, Long category, String author, Double minPrice,
			Double maxPrice) {
		// TODO: implement filtering and pagination
		return null;
	}

   
    @Override
    @Transactional
    public BookDto updateBook(Long bookId, UpdateBookRequestDTO request) {
       
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + bookId));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("User not authenticated");
        }
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User authUser = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new UnauthorizedException("Authenticated user not found"));
        
        boolean isOwner = book.getSeller() != null && book.getSeller().getId().equals(authUser.getId());
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()) || "ADMIN".equals(a.getAuthority()));

        if (!isOwner && !isAdmin) {
            throw new UnauthorizedException("Only book owner or admin can update this book");
        }

        if (request.getStock() != null && request.getStock() < 0) {
            throw new IllegalArgumentException("Stock cannot be negative.");
        }

        if (request.getPrice() != null && request.getPrice() <= 0) {
            throw new IllegalArgumentException("Price must be positive.");
        }

        if (request.getTitle() != null) book.setTitle(request.getTitle().trim());
        if (request.getAuthor() != null) book.setAuthor(request.getAuthor().trim());
        if (request.getDescription() != null) book.setDescription(request.getDescription());
        if (request.getPrice() != null) book.setPrice(request.getPrice());
        if (request.getStock() != null) book.setStock(request.getStock());
        if (request.getCondition() != null) book.setCondition(request.getCondition());
        if (request.getImageUrl() != null) book.setImageUrl(request.getImageUrl());
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with id: " + request.getCategoryId()));
        book.setCategory(category);
        Book updated = bookRepository.save(book);
        BookDto dto = modelMapper.map(updated, BookDto.class);
        dto.setCategoryId(updated.getCategory().getId());
        dto.setSellerId(updated.getSeller().getId());
        return dto;
    }

    @Override
    @Transactional
    public void deleteBook(Long bookId) {
        
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + bookId));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("User not authenticated");
        }
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User authUser = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new UnauthorizedException("Authenticated user not found"));

        boolean isOwner = book.getSeller() != null && book.getSeller().getId().equals(authUser.getId());
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()) || "ADMIN".equals(a.getAuthority()));

        if (!isOwner && !isAdmin) {
            throw new UnauthorizedException("Only book owner or admin can delete this book");
        }
        
        book.setActive(false);
        bookRepository.save(book);
    }


  
	@Override
	public BookDto getBookById(Long bookId) {
		Book book = bookRepository.findById(bookId)
				.orElseThrow(() -> new ResourceNotFoundException("Book not found with id " + bookId));

		return new BookDto(book.getId(), book.getTitle(), book.getAuthor(), book.getDescription(), book.getPrice(),
				book.getIsbn(), book.getStock(), book.getCondition(), book.getImageUrl(), book.getCategory().getId());
	}

	
	
	
	 @Override
	    public List<BookDto> getBooksByCategory(String categoryName) {
	        List<Book> books = bookRepository.findByCategory_Name(categoryName);


	        if (books.isEmpty()) {
	            throw new ResourceNotFoundException(
	                    "No books found for category: " + categoryName);
	        }
     
	        return books.stream()
	                .map(book -> modelMapper.map(book, BookDto.class))
	                .toList();
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
		// TODO
		return null;
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

	@Override
	public List<BookDto> searchBooks(SearchBooksRequestDTO request) {
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<BookDto> sortBooks(String sortBy) {
		// TODO Auto-generated method stub
		return null;
	}
}
