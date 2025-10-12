package com.bookverser.BookVerse.repository;


import com.bookverser.BookVerse.entity.User;
import com.bookverser.BookVerse.entity.Book;

import com.bookverser.BookVerse.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    
	@Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderItems oi LEFT JOIN FETCH oi.book WHERE o.id = :id")
    Optional<Order> findById(@Param("id") Long id);
}

import org.springframework.stereotype.Repository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    
	@Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderItems oi LEFT JOIN FETCH oi.book WHERE o.id = :id")
    Optional<Order> findById(@Param("id") Long id);
  
    @Query("SELECT CASE WHEN COUNT(o) > 0 THEN true ELSE false END " +
           "FROM Order o JOIN o.orderItems oi " +
           "WHERE o.customer = :customer AND oi.book = :book")
    boolean existsByCustomerAndBook(@Param("customer") User customer, @Param("book") Book book);
}
