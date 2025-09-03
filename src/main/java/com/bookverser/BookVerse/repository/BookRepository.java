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
  
//	@Query("Select b from Book b where lower(b.title) Like(lower(concat('%','keyword','%d')))"+
//	     "and(:minPrice is null or b.price>:minPrice)"+
//	     "and(:minPrice is null or b.price>:minPrice)")
//	List<BookDto> searchBooks(String keyword, Double minPrice, Double maxPrice);
//   
	

	@Query("SELECT b FROM Book b " +
		       "WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
		       "AND (:minPrice IS NULL OR b.price >= :minPrice) " +
		       "AND (:maxPrice IS NULL OR b.price <= :maxPrice)")
		List<BookDto> searchBooks(@Param("keyword") String keyword,
		                       @Param("minPrice") Double minPrice,
		                       @Param("maxPrice") Double maxPrice);
}
