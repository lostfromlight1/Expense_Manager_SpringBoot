package com.talent.expensemanager.service;

import com.talent.expensemanager.model.Category;
import com.talent.expensemanager.model.enums.TransactionType;
import java.util.List;

public interface CategoryService {
    Category createCategory(String name, TransactionType type, String currentUserId);

    List<Category> getAllCategories();

    List<Category> getCategoriesByType(TransactionType type);

    void deleteCategory(Long id, String currentUserId);

    Category updateCategory(Long id, String name, TransactionType type, String currentUserId);
}