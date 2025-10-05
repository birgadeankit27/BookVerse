package com.bookverser.BookVerse.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.bookverser.BookVerse.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomerId(Long customerId);
}

