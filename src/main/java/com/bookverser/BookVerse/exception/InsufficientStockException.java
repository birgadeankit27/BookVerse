package com.bookverser.BookVerse.exception;

public class InsufficientStockException extends RuntimeException {
	public InsufficientStockException(String message) {
        super(message);
    }
}
