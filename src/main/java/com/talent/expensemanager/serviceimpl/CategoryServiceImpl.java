package com.talent.expensemanager.serviceimpl;

import com.talent.expensemanager.exceptions.ResourceNotFoundException;
import com.talent.expensemanager.model.Category;
import com.talent.expensemanager.model.enums.TransactionType;
import com.talent.expensemanager.repository.CategoryRepository;
import com.talent.expensemanager.service.AuditService;
import com.talent.expensemanager.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CategoryServiceImpl.class);
    private final CategoryRepository categoryRepository;
    private final AuditService auditService;

    @Override
    @Transactional
    public Category createCategory(String name, TransactionType type, String currentUserId) {
        LOGGER.info("User {} creating new category: {} ({})", currentUserId, name, type);

        Category category = new Category();
        category.setName(name);
        category.setTransactionType(type);
        category.setActive(true);

        Category saved = categoryRepository.save(category);

        auditService.log("CATEGORY_CREATED", "Category", saved.getCategoryId().toString(),
                "Created " + type + " category: " + name, currentUserId);

        return saved;
    }

    @Override
    public List<Category> getAllCategories() {
        LOGGER.debug("Fetching all active categories");
        return categoryRepository.findAll().stream()
                .filter(Category::isActive)
                .collect(Collectors.toList());
    }

    @Override
    public List<Category> getCategoriesByType(TransactionType type) {
        LOGGER.debug("Fetching categories for type: {}", type);
        return categoryRepository.findAll().stream()
                .filter(c -> c.isActive() && c.getTransactionType() == type)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Category updateCategory(Long id, String name, TransactionType type, String currentUserId) {
        LOGGER.info("User {} updating category ID: {}", currentUserId, id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));

        category.setName(name);
        category.setTransactionType(type);
        Category updated = categoryRepository.save(category);

        auditService.log("CATEGORY_UPDATED", "Category", id.toString(),
                "Updated to name: " + name + " and type: " + type, currentUserId);

        return updated;
    }

    @Override
    @Transactional
    public void deleteCategory(Long id, String currentUserId) {
        LOGGER.info("User {} soft-deleting category ID: {}", currentUserId, id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));

        category.setActive(false);
        categoryRepository.save(category);

        auditService.log("CATEGORY_DELETED", "Category", id.toString(),
                "Category marked as inactive", currentUserId);
    }
}