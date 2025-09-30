package com.bookverser.BookVerse.exception;

public class EmptyCartException extends RuntimeException {
	public EmptyCartException(String message) {
        super(message);
    }
}
