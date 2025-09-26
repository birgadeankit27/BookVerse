package com.bookverser.BookVerse.repository;

import com.bookverser.BookVerse.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
