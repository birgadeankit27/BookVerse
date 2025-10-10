package com.bookverser.BookVerse.service;

import java.util.List;

import com.bookverser.BookVerse.entity.Category;

public interface CategoryService {
	Category createCategory(Category category);

    List<Category> getAllCategories();

    Category getCategoryById(Long id);

    Category updateCategory(Long id, Category updatedCategory);

    void deleteCategory(Long id); // soft delete

    void hardDelete(Long id); 
}
