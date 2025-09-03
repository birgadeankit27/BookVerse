package com.bookverser.BookVerse.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;



@Entity
@Table(name = "transactions")

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Transaction {

    // ---------- ENUMS ----------
    public enum PaymentMethod {
        COD,
        ONLINE
    }

    public enum TransactionStatus {
        SUCCESS,
        FAILED
    }

    // ---------- FIELDS ----------
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;

    @NotNull(message = "Amount is required")
    private BigDecimal amount;

    @Column(unique = true)
    private String transactionId;  // For online payments (Razorpay, PayPal, Stripe, etc.)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    @PastOrPresent(message = "Created date cannot be in the future")
    private LocalDateTime createdAt;

    // ---------- LIFECYCLE CALLBACK ----------
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}