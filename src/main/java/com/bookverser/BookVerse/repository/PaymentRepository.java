package com.bookverser.BookVerse.repository;

import com.bookverser.BookVerse.entity.Order;
import com.bookverser.BookVerse.entity.Payment;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
	  List<Payment> findByOrder_Customer_Id(Long customerId);

	  // Add this method to find payment by order ID
	    Optional<Payment> findByOrder_Id(Long orderId);
	    
	    @Query("""
	            SELECT p FROM Payment p
	            WHERE (:status IS NULL OR p.paymentStatus = :status)
	              AND (:fromDate IS NULL OR p.order.createdAt >= :fromDate)
	              AND (:toDate IS NULL OR p.order.createdAt <= :toDate)
	              AND (:email IS NULL OR p.order.customer.email = :email)
	        """)
	        Page<Payment> findAllWithFilters(
	                @Param("status") Order.PaymentStatus status,
	                @Param("fromDate") LocalDateTime fromDate,
	                @Param("toDate") LocalDateTime toDate,
	                @Param("email") String email,
	                Pageable pageable
	        );
}
