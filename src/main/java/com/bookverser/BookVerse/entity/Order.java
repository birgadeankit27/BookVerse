package com.bookverser.BookVerse.entity;
import jakarta.persistence.*;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Order {
  
    public enum Status {
        PENDING,
        SHIPPED,
        DELIVERED,
        CANCELLED
    }

    public enum PaymentStatus {
        PAID,
        COD,
        FAILED
    }

  
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    private User buyer;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus = PaymentStatus.COD;

    @PastOrPresent(message = "Created date cannot be in the future")
    private LocalDateTime createdAt;

  
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

   
}
