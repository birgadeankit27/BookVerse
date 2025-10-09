package com.bookverser.BookVerse.repository;


import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    
	@Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderItems oi LEFT JOIN FETCH oi.book WHERE o.id = :id")
    Optional<Order> findById(@Param("id") Long id);
  
    @Query("SELECT CASE WHEN COUNT(o) > 0 THEN true ELSE false END " +
           "FROM Order o JOIN o.orderItems oi " +
           "WHERE o.customer = :customer AND oi.book = :book")
    boolean existsByCustomerAndBook(@Param("customer") User customer, @Param("book") Book book);
}
