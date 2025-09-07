package com.bookverser.BookVerse.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalException {
	
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
}
