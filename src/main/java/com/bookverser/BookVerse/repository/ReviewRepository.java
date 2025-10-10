package com.bookverser.BookVerse.repository;

import com.bookverser.BookVerse.entity.Review;
import com.bookverser.BookVerse.entity.Book;
import com.bookverser.BookVerse.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    boolean existsByBookAndUser(Book book, User user);

    List<Review> findByBook(Book book);
}
