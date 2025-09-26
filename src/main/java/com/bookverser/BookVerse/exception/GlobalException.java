package com.bookverser.BookVerse.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalException {
    // Resource not found → 404
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<String> handleResourceNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    
    @ExceptionHandler(DuplicateIsbnException.class)
	public ResponseEntity<Map<String, String>> handleDuplicateIsbn(DuplicateIsbnException ex) {
		Map<String, String> error = new HashMap<>();
		error.put("error", ex.getMessage());
		return new ResponseEntity<>(error, HttpStatus.CONFLICT); // 409
	}

	@ExceptionHandler(UsernameNotFoundException.class)
	public ResponseEntity<String> handleUsernameNotFoundException(UsernameNotFoundException ex) {
		return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<String> handleValidationException(MethodArgumentNotValidException ex) {
		return new ResponseEntity<>("Validation failed: " + ex.getMessage(), HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(UnauthorizedException.class)
	public ResponseEntity<String> handleUnauthorizedException(UnauthorizedException ex) {
		return new ResponseEntity<>(ex.getMessage(), HttpStatus.FORBIDDEN);
	}

//	@ExceptionHandler(ResourceNotFoundException.class)
//	public ResponseEntity<String> resourceNotFoundException(ResourceNotFoundException ex) {
//		return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
//	}

	@ExceptionHandler(InvalidPriceRangeException.class)
	public ResponseEntity<String> handleInvalidPriceRange(InvalidPriceRangeException ex) {
		return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(CategoryNotFoundException.class)
	public ResponseEntity<String> handleCategoryNotFound(CategoryNotFoundException ex) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
	}

	@ExceptionHandler(InvalidSortParameterException.class)
	public ResponseEntity<String> handleInvalidSortParameter(InvalidSortParameterException ex) {
	    return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(InvalidRequestException.class)
	public ResponseEntity<String> handleInvalidRequest(InvalidRequestException ex) {
	    return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
	}

	  @ExceptionHandler(BookNotFoundException.class)
	    public ResponseEntity<String> handleBookNotFound(BookNotFoundException ex) {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
	    }

	    @ExceptionHandler(InvalidQuantityException.class)
	    public ResponseEntity<String> handleInvalidQuantity(InvalidQuantityException ex) {
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
	    }
          
	    @ExceptionHandler(CartItemNotFoundException.class)
	    public ResponseEntity<String> handleCartItemNotFound(CartItemNotFoundException ex) {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
	    }

	    @ExceptionHandler(OrderNotFoundException.class)
	    public ResponseEntity<String> handleOrderNotFound(OrderNotFoundException ex) {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
	    }

	    @ExceptionHandler(InvalidPaymentMethodException.class)
	    public ResponseEntity<String> handleInvalidPayment(InvalidPaymentMethodException ex) {
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
	    }

	    @ExceptionHandler(PaymentFailedException.class)
	    public ResponseEntity<String> handlePaymentFailed(PaymentFailedException ex) {
	        return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body(ex.getMessage());
	    }
	
	// Handle all other exceptions → 500
	@ExceptionHandler(Exception.class)
	public ResponseEntity<Map<String, String>> handleExceptions(Exception ex) {
		Map<String, String> error = Map.of("error", "Internal Server Error: " + ex.getMessage());
		return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
	}

}
