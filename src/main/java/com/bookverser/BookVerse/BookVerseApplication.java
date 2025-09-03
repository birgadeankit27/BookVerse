package com.bookverser.BookVerse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode;

@SpringBootApplication
@EnableSpringDataWebSupport(pageSerializationMode = PageSerializationMode.VIA_DTO)
public class BookVerseApplication {

	public static void main(String[] args) {
		SpringApplication.run(BookVerseApplication.class, args);
		System.out.println("application started");
	}

}
