package com.bookverser.BookVerse.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bookverser.BookVerse.entity.Order;

@Repository
public interface OrderRepository  extends JpaRepository<Order, Long> {

}
