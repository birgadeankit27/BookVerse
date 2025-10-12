package com.bookverser.BookVerse.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalException {

    // 1️⃣ Validation errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
            errors.put(error.getField(), error.getDefaultMessage())
        );
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    // 2️⃣ Unauthorized → 403
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Map<String, String>> handleUnauthorized(UnauthorizedException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    // 3️⃣ Not found → 404
    @ExceptionHandler({
        ResourceNotFoundException.class,
        BookNotFoundException.class,
        CartItemNotFoundException.class,
        OrderNotFoundException.class,
        CategoryNotFoundException.class,
        UsernameNotFoundException.class,
        PaymentNotFoundException.class
    })
    public ResponseEntity<Map<String, String>> handleNotFound(Exception ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    // 4️⃣ Bad request → 400
    @ExceptionHandler({
        InvalidQuantityException.class,
        InvalidPriceRangeException.class,
        InvalidSortParameterException.class,
        InvalidRequestException.class,
        InvalidPaymentMethodException.class,
        InsufficientStockException.class,
        InvalidAddressException.class,
        RefundNotAllowedException.class,
        InvalidCategoryNameException.class,
        CategoryAlreadyExistsException.class
    })
    public ResponseEntity<Map<String, String>> handleBadRequest(Exception ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // 5️⃣ Conflict → 409
    @ExceptionHandler(DuplicateIsbnException.class)
    public ResponseEntity<Map<String, String>> handleConflict(DuplicateIsbnException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    // 6️⃣ Payment required → 402
    @ExceptionHandler(PaymentFailedException.class)
    public ResponseEntity<Map<String, String>> handlePaymentFailed(PaymentFailedException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.PAYMENT_REQUIRED);
    }

    // 7️⃣ Illegal arguments → 400
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // 8️⃣ Generic fallback → 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Internal Server Error: " + ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
