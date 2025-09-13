package com.bookverser.BookVerse.repository;


import java.util.List;

import java.util.List;
import java.util.Optional;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.bookverser.BookVerse.entity.Book;
import com.bookverser.BookVerse.entity.Category;

@Repository

public interface BookRepository extends JpaRepository<Book, Long> {

    boolean existsByIsbn(String isbn);
    
    
    @Query("SELECT b FROM Book b " +
            "WHERE (:title IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%'))) " +
            "AND (:author IS NULL OR LOWER(b.author) LIKE LOWER(CONCAT('%', :author, '%'))) " +
            "AND (:categoryName IS NULL OR LOWER(b.category.name) LIKE LOWER(CONCAT('%', :categoryName, '%'))) " +
            "AND (:isbn IS NULL OR LOWER(b.isbn) LIKE LOWER(CONCAT('%', :isbn, '%')))")
    List<Book> searchBooks(@Param("title") String title,
                           @Param("author") String author,
                           @Param("categoryName") String categoryName,
                           @Param("isbn") String isbn);
   
    
    List<Book> findBySeller_Id(Long sellerId);
    
    List<Book> findByCategory_Name(String categoryName);
    
    List<Book> findByFeaturedTrue();
   


	


}

