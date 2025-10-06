package com.bookverser.BookVerse.repository;

import com.bookverser.BookVerse.entity.Book;
import com.bookverser.BookVerse.entity.User;
import com.bookverser.BookVerse.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Query("SELECT CASE WHEN COUNT(oi) > 0 THEN true ELSE false END " +
           "FROM OrderItem oi WHERE oi.book = :book AND oi.order.customer = :customer")
    boolean existsByCustomerAndBook(@Param("customer") User customer, @Param("book") Book book);
}
