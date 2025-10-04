package com.bookverser.BookVerse.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.PastOrPresent;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Order Entity
 * Represents an order in the database, including buyer, items, and shipping details.
 * - Depends on the books table for order items (via OrderItem entity).
 * - Uses cascade and orphanRemoval to manage OrderItem lifecycle.
 */
@Entity
@Table(name = "orders")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Order {

    public enum Status {
        PENDING, SHIPPED, DELIVERED, CANCELLED
    }

    public enum PaymentStatus {
        PAID, COD, FAILED ,REFUNDED, MANUAL_REFUND_REQUIRED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> orderItems = new ArrayList<>();


    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalPrice;

    @ManyToOne(fetch = FetchType.LAZY) // JPA allows null
    @JoinColumn(name = "shipping_address_id", nullable = false) // DB column allows null
    private Address shippingAddress;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus = PaymentStatus.COD;

    @PastOrPresent(message = "Created date cannot be in the future")
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id")
    private Cart cart;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}