package com.bookverser.BookVerse.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.bookverser.BookVerse.entity.Book;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    // ✅ Unique ISBN check
    boolean existsByIsbn(String isbn);

    // ------------------- Search by multiple fields -------------------
    @Query("SELECT b FROM Book b " +
            "WHERE (:title IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%'))) " +
            "AND (:author IS NULL OR LOWER(b.author) LIKE LOWER(CONCAT('%', :author, '%'))) " +
            "AND (:categoryName IS NULL OR LOWER(b.category.name) LIKE LOWER(CONCAT('%', :categoryName, '%'))) " +
            "AND (:isbn IS NULL OR LOWER(b.isbn) LIKE LOWER(CONCAT('%', :isbn, '%')))")
    List<Book> searchBooks(@Param("title") String title,
                           @Param("author") String author,
                           @Param("categoryName") String categoryName,
                           @Param("isbn") String isbn);

    // ------------------- Keyword + Price Range Search -------------------
    @Query("SELECT b FROM Book b " +
           "WHERE (:keyword IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "   OR LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:minPrice IS NULL OR b.price >= :minPrice) " +
           "AND (:maxPrice IS NULL OR b.price <= :maxPrice)")
    List<Book> searchBooksByKeywordAndPrice(@Param("keyword") String keyword,
                                            @Param("minPrice") BigDecimal minPrice,
                                            @Param("maxPrice") BigDecimal maxPrice);

    // ------------------- Filter by Seller, Category, Featured -------------------
    List<Book> findBySeller_Id(Long sellerId);

    List<Book> findByCategory_Name(String categoryName);

    List<Book> findByFeaturedTrue();

    // ------------------- Advanced Filter with location -------------------
    @Query("SELECT b FROM Book b " +
            "JOIN b.category c " +
            "JOIN b.seller s " +
            "JOIN s.addresses a " +
            "WHERE (:category IS NULL OR c.name = :category) " +
            "AND (:minPrice IS NULL OR b.price >= :minPrice) " +
            "AND (:maxPrice IS NULL OR b.price <= :maxPrice) " +
            "AND (:location IS NULL OR a.city = :location)")
    List<Book> findAllFilter(@Param("category") String category,
                             @Param("minPrice") BigDecimal minPrice,
                             @Param("maxPrice") BigDecimal maxPrice,
                             @Param("location") String location);

    // ------------------- Sorting Queries -------------------
    @Query("SELECT b FROM Book b ORDER BY b.createdAt DESC")
    List<Book> findAllByLatest();

    @Query("SELECT b FROM Book b ORDER BY b.price ASC")
    List<Book> findAllByPriceAsc();

    @Query("SELECT b FROM Book b ORDER BY b.price DESC")
    List<Book> findAllByPriceDesc();

    @Query("SELECT b FROM Book b LEFT JOIN Review r ON r.book = b " +
           "GROUP BY b.id ORDER BY COALESCE(AVG(r.rating), 0) DESC")
    List<Book> findAllByRating();

}
