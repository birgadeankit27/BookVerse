package com.bookverser.BookVerse.serviceimpl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.bookverser.BookVerse.entity.Category;
import com.bookverser.BookVerse.exception.CategoryAlreadyExistsException;
import com.bookverser.BookVerse.exception.CategoryNotFoundException;
import com.bookverser.BookVerse.exception.InvalidCategoryNameException;
import com.bookverser.BookVerse.repository.CategoryRepository;
import com.bookverser.BookVerse.service.CategoryService;

import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
  
	private final CategoryRepository categoryRepository;

	@Override
	public Category createCategory(Category category) {
		 // Prevent duplicate category names
        categoryRepository.findByName(category.getName())
                .ifPresent(c -> { 
                    throw new CategoryAlreadyExistsException("Category already exists with name: " + category.getName()); 
                });

        // Validate category name format
        if (!category.getName().matches("^[A-Za-z ]+$")) {
            throw new InvalidCategoryNameException("Category name should only contain letters and spaces");
        }

        // Mark as active by default
        category.setActive(true);
        return categoryRepository.save(category);
	}

	@Override
	public List<Category> getAllCategories() {
		// TODO Auto-generated method stub
		return categoryRepository.findByIsActiveTrue();
	}

	@Override
	public Category getCategoryById(Long id) {
		  return categoryRepository.findById(id)
	                .orElseThrow(() -> new CategoryNotFoundException("Category not found with id " + id));
	    }
	

	@Override
	public Category updateCategory(Long id, Category updatedCategory) {
		Category category = getCategoryById(id);

        // Prevent rename to duplicate name
        categoryRepository.findByName(updatedCategory.getName()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new CategoryAlreadyExistsException("Another category with this name already exists");
            }
        });

        // Validate format
        if (!updatedCategory.getName().matches("^[A-Za-z ]+$")) {
            throw new InvalidCategoryNameException("Category name should only contain letters and spaces");
        }

        category.setName(updatedCategory.getName());
        category.setDescription(updatedCategory.getDescription());
        category.setActive(updatedCategory.isActive());
        return categoryRepository.save(category);
	}

	@Override
	public void deleteCategory(Long id) {
		 Category category = getCategoryById(id);

	        // If category has books → block deletion
	        if (category.getBooks() != null && !category.getBooks().isEmpty()) {
	            throw new IllegalStateException("Cannot delete category because books are assigned to it");
	        }

	        category.setActive(false);
	        categoryRepository.save(category);		
	}

	@Override
	public void hardDelete(Long id) {
		  Category category = getCategoryById(id);

	        // Prevent delete if it has books
	        if (category.getBooks() != null && !category.getBooks().isEmpty()) {
	            throw new IllegalStateException("Cannot permanently delete category because books are assigned to it");
	        }

	        categoryRepository.deleteById(id);
	}
	
	

}
