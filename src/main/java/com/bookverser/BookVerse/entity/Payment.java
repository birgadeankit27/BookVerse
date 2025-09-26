package com.bookverser.BookVerse.entity;

import com.bookverser.BookVerse.enums.PaymentMethod;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String transactionId;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    private Order.PaymentStatus paymentStatus; // use existing enum from Order

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
}
