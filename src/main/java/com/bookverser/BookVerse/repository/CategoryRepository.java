package com.bookverser.BookVerse.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bookverser.BookVerse.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long>{

}
