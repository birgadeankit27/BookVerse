package com.bookverser.BookVerse.repository;
import java.util.List;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.bookverser.BookVerse.dto.BookDto;
import com.bookverser.BookVerse.entity.Book;

@Repository
public interface BookRepository extends JpaRepository<Book, Long>{
	
	
	 boolean existsByIsbn(String isbn);

	    @Query("SELECT b FROM Book b " +
	           "WHERE (:keyword IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
	           "OR LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
	           "AND (:minPrice IS NULL OR b.price >= :minPrice) " +
	           "AND (:maxPrice IS NULL OR b.price <= :maxPrice)")
	    List<Book> searchBooks(@Param("keyword") String keyword,
	                           @Param("minPrice") Double minPrice,
	                           @Param("maxPrice") Double maxPrice);


	
}
