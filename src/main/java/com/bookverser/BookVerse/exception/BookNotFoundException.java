package com.bookverser.BookVerse.exception;

public class BookNotFoundException extends RuntimeException{
	public BookNotFoundException(String message) {
        super(message);
    }
}
