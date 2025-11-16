//package com.bookverser.BookVerse.exception;
//import java.util.HashMap;
//import java.util.Map;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.MethodArgumentNotValidException;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.web.bind.annotation.RestControllerAdvice;
//
//@RestControllerAdvice
//public class GlobalException {
//
//    // Duplicate ISBN → 409
//    @ExceptionHandler(DuplicateIsbnException.class)
//    public ResponseEntity<Map<String, String>> handleDuplicateIsbn(DuplicateIsbnException ex) {
//        return new ResponseEntity<>(Map.of("error", ex.getMessage()), HttpStatus.CONFLICT);
//    }
//
//    // Unauthorized access → 403
//    @ExceptionHandler(UnauthorizedException.class)
//    public ResponseEntity<Map<String, String>> handleUnauthorized(UnauthorizedException ex) {
//        return new ResponseEntity<>(Map.of("error", ex.getMessage()), HttpStatus.FORBIDDEN);
//    }
//
//    // Not found (combine all your not found exceptions here) → 404
//    @ExceptionHandler({
//        ResourceNotFoundException.class,
//        BookNotFoundException.class,
//        CartItemNotFoundException.class,
//        OrderNotFoundException.class,
//        CategoryNotFoundException.class,
//        UsernameNotFoundException.class
//    })
//    public ResponseEntity<Map<String, String>> handleNotFound(Exception ex) {
//        return new ResponseEntity<>(Map.of("error", ex.getMessage()), HttpStatus.NOT_FOUND);
//    }
//
//    // Validation errors (from @Valid) → 400
//    @ExceptionHandler(MethodArgumentNotValidException.class)
//    public ResponseEntity<Map<String, String>> handleValidationErrors(MethodArgumentNotValidException ex) {
//        Map<String, String> errors = new HashMap<>();
//        ex.getBindingResult().getFieldErrors().forEach(error ->
//                errors.put(error.getField(), error.getDefaultMessage()));
//        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
//    }
//
//    // Bad request / invalid input → 400
//    @ExceptionHandler({
//        InvalidQuantityException.class,
//        InvalidPriceRangeException.class,
//        InvalidSortParameterException.class,
//        InvalidRequestException.class,
//        InvalidPaymentMethodException.class,
//        InsufficientStockException.class,
//        InvalidAddressException.class
//    })
//    public ResponseEntity<Map<String, String>> handleBadRequest(Exception ex) {
//        return new ResponseEntity<>(Map.of("error", ex.getMessage()), HttpStatus.BAD_REQUEST);
//    }
//
//    // Payment failures → 402
//    @ExceptionHandler(PaymentFailedException.class)
//    public ResponseEntity<Map<String, String>> handlePaymentFailed(PaymentFailedException ex) {
//        return new ResponseEntity<>(Map.of("error", ex.getMessage()), HttpStatus.PAYMENT_REQUIRED);
//    }
//
//    // Illegal arguments → 400
//    @ExceptionHandler(IllegalArgumentException.class)
//    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
//        return new ResponseEntity<>(Map.of("error", ex.getMessage()), HttpStatus.BAD_REQUEST);
//    }
//
//    // Generic fallback → 500
//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<Map<String, String>> handleGenericException(Exception ex) {
//        return new ResponseEntity<>(Map.of("error", "Internal Server Error: " + ex.getMessage()),
//                                    HttpStatus.INTERNAL_SERVER_ERROR);
//    }
//}



package com.bookverser.BookVerse.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /* ------------------------------------------------------------
       1. VALIDATION ERRORS (@Valid)
       ------------------------------------------------------------ */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }


    /* ------------------------------------------------------------
       2. NOT FOUND ERRORS (404)
       ------------------------------------------------------------ */
    @ExceptionHandler({
            ResourceNotFoundException.class,
            BookNotFoundException.class,
            CartItemNotFoundException.class,
            OrderNotFoundException.class,
            CategoryNotFoundException.class,
            UsernameNotFoundException.class
    })
    public ResponseEntity<Map<String, String>> handleNotFound(Exception ex) {
        return new ResponseEntity<>(Map.of("error", ex.getMessage()), HttpStatus.NOT_FOUND);
    }


    /* ------------------------------------------------------------
       3. BAD REQUEST ERRORS (400)
       ------------------------------------------------------------ */
    @ExceptionHandler({
            InvalidQuantityException.class,
            InvalidPriceRangeException.class,
            InvalidSortParameterException.class,
            InvalidRequestException.class,
            InvalidPaymentMethodException.class,
            InsufficientStockException.class,
            InvalidAddressException.class,
            IllegalArgumentException.class // added here
    })
    public ResponseEntity<Map<String, String>> handleBadRequest(Exception ex) {
        return new ResponseEntity<>(Map.of("error", ex.getMessage()), HttpStatus.BAD_REQUEST);
    }


    /* ------------------------------------------------------------
       4. RUNTIME EXCEPTIONS (400) – VERY IMPORTANT FOR REGISTER API
       ------------------------------------------------------------ */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntime(RuntimeException ex) {
        return new ResponseEntity<>(Map.of("error", ex.getMessage()), HttpStatus.BAD_REQUEST);
    }


    /* ------------------------------------------------------------
       5. CONFLICT ERRORS (409)
       ------------------------------------------------------------ */
    @ExceptionHandler(DuplicateIsbnException.class)
    public ResponseEntity<Map<String, String>> handleConflict(DuplicateIsbnException ex) {
        return new ResponseEntity<>(Map.of("error", ex.getMessage()), HttpStatus.CONFLICT);
    }


    /* ------------------------------------------------------------
       6. UNAUTHORIZED / FORBIDDEN (403)
       ------------------------------------------------------------ */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Map<String, String>> handleUnauthorized(UnauthorizedException ex) {
        return new ResponseEntity<>(Map.of("error", ex.getMessage()), HttpStatus.FORBIDDEN);
    }


    /* ------------------------------------------------------------
       7. PAYMENT FAILED (402)
       ------------------------------------------------------------ */
    @ExceptionHandler(PaymentFailedException.class)
    public ResponseEntity<Map<String, String>> handlePaymentFailed(PaymentFailedException ex) {
        return new ResponseEntity<>(Map.of("error", ex.getMessage()), HttpStatus.PAYMENT_REQUIRED);
    }


    /* ------------------------------------------------------------
       8. FALLBACK FOR ANY OTHER ERROR (500)
       ------------------------------------------------------------ */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception ex) {

        ex.printStackTrace(); // log the error to console

        return new ResponseEntity<>(
                Map.of("error", "Internal Server Error: " + ex.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}
