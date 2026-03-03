package com.commerce_pro_backend.catalog.category.controller;

import com.commerce_pro_backend.catalog.category.dto.CategoryDto;
import com.commerce_pro_backend.catalog.category.service.CategoryService;
import com.commerce_pro_backend.common.dto.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    // === TREE & HIERARCHY ENDPOINTS ===

    @GetMapping("/tree")
    public ResponseEntity<ApiResponse<List<CategoryDto.TreeResponse>>> getTree(
            @RequestParam(required = false) String tenantId) {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getCategoryTree(tenantId)));
    }

    @GetMapping("/menu")
    public ResponseEntity<ApiResponse<List<CategoryDto.Response>>> getMenuCategories(
            @RequestParam(required = false) String tenantId) {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getMenuCategories(tenantId)));
    }

    @GetMapping("/level/{level}")
    public ResponseEntity<ApiResponse<List<CategoryDto.Response>>> getByLevel(
            @PathVariable Integer level,
            @RequestParam(required = false) String tenantId) {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getCategoriesByLevel(level, tenantId)));
    }

    // === CRUD ENDPOINTS ===

    @GetMapping
    public ResponseEntity<ApiResponse<Page<CategoryDto.Response>>> getAll(
            @RequestParam(required = false) String tenantId,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getAllCategories(tenantId, pageable)));
    }

    @GetMapping("/deleted")
    public ResponseEntity<ApiResponse<Page<CategoryDto.Response>>> getDeleted(
            @RequestParam(required = false) String tenantId,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getDeletedCategories(tenantId, pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryDto.Response>> getById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getCategory(id)));
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<ApiResponse<CategoryDto.Response>> getBySlug(
            @PathVariable String slug,
            @RequestParam(required = false) String tenantId) {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getCategoryBySlug(slug, tenantId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CategoryDto.Response>> create(
            @Valid @RequestBody CategoryDto.Request request,
            Authentication authentication) {
        String currentUserId = getUserIdFromAuthentication(authentication);
        String tenantId = getTenantIdFromAuthentication(authentication);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(categoryService.createCategory(request, currentUserId, tenantId)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryDto.Response>> update(
            @PathVariable String id,
            @Valid @RequestBody CategoryDto.Request request,
            Authentication authentication) {
        String currentUserId = getUserIdFromAuthentication(authentication);
        return ResponseEntity.ok(ApiResponse.success(categoryService.updateCategory(id, request, currentUserId)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> delete(
            @PathVariable String id,
            Authentication authentication) {
        String currentUserId = getUserIdFromAuthentication(authentication);
        categoryService.deleteCategory(id, currentUserId);
        return ResponseEntity.ok(ApiResponse.success("Category deleted successfully"));
    }

    @PostMapping("/{id}/restore")
    public ResponseEntity<ApiResponse<CategoryDto.Response>> restore(
            @PathVariable String id,
            Authentication authentication) {
        String currentUserId = getUserIdFromAuthentication(authentication);
        return ResponseEntity.ok(ApiResponse.success(categoryService.restoreCategory(id, currentUserId)));
    }

    // === HIERARCHY MANAGEMENT ENDPOINTS ===

    @GetMapping("/{id}/subcategories")
    public ResponseEntity<ApiResponse<List<CategoryDto.Response>>> getSubcategories(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getSubcategories(id)));
    }

    @PostMapping("/{id}/move")
    public ResponseEntity<ApiResponse<CategoryDto.Response>> move(
            @PathVariable String id,
            @Valid @RequestBody CategoryDto.MoveRequest request,
            Authentication authentication) {
        String currentUserId = getUserIdFromAuthentication(authentication);
        return ResponseEntity.ok(ApiResponse.success(categoryService.moveCategory(id, request.getNewParentId(), currentUserId)));
    }

    @PostMapping("/{id}/reorder")
    public ResponseEntity<ApiResponse<String>> reorder(
            @PathVariable String id,
            @Valid @RequestBody CategoryDto.ReorderRequest request,
            Authentication authentication) {
        String currentUserId = getUserIdFromAuthentication(authentication);
        categoryService.reorderCategory(id, request.getNewSortOrder(), currentUserId);
        return ResponseEntity.ok(ApiResponse.success("Category reordered successfully"));
    }

    @GetMapping("/{id}/path")
    public ResponseEntity<ApiResponse<List<CategoryDto.Response>>> getPath(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getCategoryPath(id)));
    }

    @GetMapping("/{id}/subtree")
    public ResponseEntity<ApiResponse<List<CategoryDto.Response>>> getSubtree(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getCategorySubtree(id)));
    }

    @GetMapping("/{id}/descendants")
    public ResponseEntity<ApiResponse<List<String>>> getDescendants(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getAllSubcategoryIds(id)));
    }

    @GetMapping("/{id}/descendants/count")
    public ResponseEntity<ApiResponse<Long>> countDescendants(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(categoryService.countDescendants(id)));
    }

    // === CUSTOM FIELDS ENDPOINTS ===

    @GetMapping("/{id}/custom-fields")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCustomFields(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getCustomFields(id)));
    }

    @PutMapping("/{id}/custom-fields")
    public ResponseEntity<ApiResponse<String>> updateCustomFields(
            @PathVariable String id,
            @Valid @RequestBody CategoryDto.CustomFieldUpdateRequest request,
            Authentication authentication) {
        String currentUserId = getUserIdFromAuthentication(authentication);
        categoryService.updateCustomFields(id, request.getFields(), currentUserId);
        return ResponseEntity.ok(ApiResponse.success("Custom fields updated successfully"));
    }

    // === STATUS MANAGEMENT ENDPOINTS ===

    @PatchMapping("/{id}/active")
    public ResponseEntity<ApiResponse<String>> toggleActive(
            @PathVariable String id,
            @RequestParam boolean active,
            Authentication authentication) {
        String currentUserId = getUserIdFromAuthentication(authentication);
        categoryService.toggleActive(id, active, currentUserId);
        return ResponseEntity.ok(ApiResponse.success("Active status updated"));
    }

    // === STATISTICS ENDPOINTS ===

    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getStatistics(
            @RequestParam(required = false) String tenantId) {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getStatistics(tenantId)));
    }

    // === PRIVATE HELPER METHODS ===

    private String getUserIdFromAuthentication(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }
        return authentication.getName(); // or extract from JWT principal
    }

    private String getTenantIdFromAuthentication(Authentication authentication) {
        if (authentication == null) {
            return null;
        }
        // Extract tenantId from JWT claims or user details
        // This depends on your JWT structure
        return null; // implement based on your auth structure
    }
}