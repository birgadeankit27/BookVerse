package com.bookverser.BookVerse.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Category Entity
 * Represents a book category in the database.
 * ✅ Suggestions Added:
 * - Soft delete support (isActive flag)
 * - PrePersist/PreUpdate methods for custom handling
 * - Better column constraints
 * - Safe list initialization
 */
@Entity
@Table(name = "categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Category name is mandatory")
    @Size(min = 2, max = 100, message = "Category name must be between 2 and 100 characters")
    @Column(unique = true, nullable = false, length = 100)
    private String name;

    @Size(max = 500, message = "Description must be less than 500 characters")
    @Column(length = 500)
    private String description;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private boolean isActive = true; // ✅ Soft delete support

 // Do NOT serialize books to JSON → prevents infinite recursion
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Book> books = new ArrayList<>();
    @PrePersist
    public void prePersist() {
        this.isActive = true; // ensures category is active by default
    }

    @PreUpdate
    public void preUpdate() {
        // You could add audit logs or validations here if needed
    }
}
