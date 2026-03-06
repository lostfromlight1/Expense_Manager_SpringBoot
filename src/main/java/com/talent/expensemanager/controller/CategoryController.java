package com.talent.expensemanager.controller;

import com.talent.expensemanager.model.Category;
import com.talent.expensemanager.model.enums.TransactionType;
import com.talent.expensemanager.response.BaseResponse;
import com.talent.expensemanager.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private static final Logger LOGGER = LoggerFactory.getLogger(CategoryController.class);
    private final CategoryService categoryService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BaseResponse<Category>> createCategory(
            @RequestParam String name,
            @RequestParam TransactionType type,
            Authentication authentication) {

        String currentUserId = (String) authentication.getPrincipal();
        Category category = categoryService.createCategory(name, type, currentUserId);

        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.<Category>builder()
                .httpStatusCode(HttpStatus.CREATED.value())
                .apiName("CREATE_CATEGORY")
                .apiId("CAT-001")
                .traceId(MDC.get("correlationId"))
                .message("Category created successfully")
                .data(category)
                .build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BaseResponse<Category>> updateCategory(
            @PathVariable Long id,
            @RequestParam String name,
            @RequestParam TransactionType type,
            Authentication authentication) {

        String currentUserId = (String) authentication.getPrincipal();
        Category updated = categoryService.updateCategory(id, name, type, currentUserId);

        return ResponseEntity.ok(BaseResponse.<Category>builder()
                .httpStatusCode(HttpStatus.OK.value())
                .apiName("UPDATE_CATEGORY")
                .apiId("CAT-002")
                .traceId(MDC.get("correlationId"))
                .message("Category updated successfully")
                .data(updated)
                .build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BaseResponse<Void>> deleteCategory(
            @PathVariable Long id,
            Authentication authentication) {

        String currentUserId = (String) authentication.getPrincipal();
        categoryService.deleteCategory(id, currentUserId);

        return ResponseEntity.ok(BaseResponse.<Void>builder()
                .httpStatusCode(HttpStatus.OK.value())
                .apiName("DELETE_CATEGORY")
                .apiId("CAT-003")
                .traceId(MDC.get("correlationId"))
                .message("Category deleted successfully")
                .build());
    }

    @GetMapping
    public ResponseEntity<BaseResponse<List<Category>>> getAll() {
        return ResponseEntity.ok(BaseResponse.<List<Category>>builder()
                .httpStatusCode(HttpStatus.OK.value())
                .apiName("GET_ALL_CATEGORIES")
                .apiId("CAT-004")
                .traceId(MDC.get("correlationId"))
                .data(categoryService.getAllCategories())
                .build());
    }

    @GetMapping("/filter")
    public ResponseEntity<BaseResponse<List<Category>>> getByType(@RequestParam TransactionType type) {
        return ResponseEntity.ok(BaseResponse.<List<Category>>builder()
                .httpStatusCode(HttpStatus.OK.value())
                .apiName("GET_CATEGORIES_BY_TYPE")
                .apiId("CAT-005")
                .traceId(MDC.get("correlationId"))
                .data(categoryService.getCategoriesByType(type))
                .build());
    }
}