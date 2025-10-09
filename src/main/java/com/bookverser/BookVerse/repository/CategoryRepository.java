package com.bookverser.BookVerse.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bookverser.BookVerse.entity.Category;
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long>{
	Optional<Category> findByName(String name);

    List<Category> findByIsActiveTrue();
}
