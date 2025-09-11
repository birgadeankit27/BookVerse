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
	 List<Book> findByCategory_Name(String categoryName);@Query("SELECT b FROM Book b " +
	           "WHERE (LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
	           "   OR LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
	           "   OR LOWER(b.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) ")
	    List<Book> searchByKeyword(@Param("keyword") String keyword);
	 
	 @Query("SELECT b FROM Book b " +
		       "WHERE (:title IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%'))) " +
		       "AND (:author IS NULL OR LOWER(b.author) LIKE LOWER(CONCAT('%', :author, '%'))) " +
		       "AND (:isbn IS NULL OR b.isbn = :isbn)")
		List<Book> searchByTitleAuthorIsbn(@Param("title") String title,
		                                   @Param("author") String author,
		                                   @Param("isbn") String isbn);
	 
	 @Query("SELECT b FROM Book b " +
		       "WHERE (:category IS NULL OR LOWER(b.category.name) = LOWER(:category)) " +
		       "AND (:minPrice IS NULL OR b.price >= :minPrice) " +
		       "AND (:maxPrice IS NULL OR b.price <= :maxPrice) " +
		       "AND (:location IS NULL OR LOWER(b.seller.address) LIKE LOWER(CONCAT('%', :location, '%')))")
		List<Book> filterBooks(@Param("category") String category,
		                       @Param("minPrice") Double minPrice,
		                       @Param("maxPrice") Double maxPrice,
		                       @Param("location") String location);
	 
	 @Query("SELECT b FROM Book b " +
		       "WHERE (:keyword IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
		       "   OR LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
		       "   OR LOWER(b.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
		       "AND (:minPrice IS NULL OR b.price >= :minPrice) " +
		       "AND (:maxPrice IS NULL OR b.price <= :maxPrice)")
		List<Book> searchBooksWithFilters(@Param("keyword") String keyword,
		                                  @Param("minPrice") Double minPrice,
		                                  @Param("maxPrice") Double maxPrice);
	 
	    
	    List<Book> findByTitleContainingIgnoreCase(String title);
	    
	    List<Book> findByAuthorContainingIgnoreCase(String author);
	    
	    List<Book> findByCategoryNameIgnoreCase(String categoryName);
	    
	    Optional<Book> findByIsbn(String isbn); 
	 
	
	 


}
