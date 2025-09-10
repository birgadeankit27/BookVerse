package com.bookverser.BookVerse.repository;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.bookverser.BookVerse.entity.Book;
import com.bookverser.BookVerse.entity.Category;

@Repository
public interface BookRepository extends JpaRepository<Book, Long>{
	
	
	 boolean existsByIsbn(String isbn);
	 List<Book> findByCategory_Name(String categoryName);


}
