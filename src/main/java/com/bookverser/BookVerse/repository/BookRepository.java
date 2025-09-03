package com.bookverser.BookVerse.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bookverser.BookVerse.entity.Book;

@Repository
public interface BookRepository extends JpaRepository<Book, Long>{

}
