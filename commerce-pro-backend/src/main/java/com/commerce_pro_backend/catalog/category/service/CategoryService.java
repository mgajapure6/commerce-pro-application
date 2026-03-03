package com.commerce_pro_backend.catalog.category.service;

import com.commerce_pro_backend.catalog.category.dto.CategoryDto;
import com.commerce_pro_backend.catalog.category.entity.Category;
import com.commerce_pro_backend.catalog.category.mapper.CategoryMapper;
import com.commerce_pro_backend.catalog.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    // === READ OPERATIONS ===

    @Cacheable(value = "categories", key = "'tree:' + #tenantId")
    public List<CategoryDto.TreeResponse> getCategoryTree(String tenantId) {
        log.debug("Fetching category tree for tenant: {}", tenantId);
        List<Category> roots = categoryRepository.findRootCategories(tenantId);
        return categoryMapper.toTreeResponseList(roots);
    }

    public CategoryDto.Response getCategory(String id) {
        log.debug("Fetching category: {}", id);
        Category category = categoryRepository.findByIdWithFullHierarchy(id)
            .orElseThrow(() -> new RuntimeException("Category not found: " + id));
        return categoryMapper.toResponse(category);
    }

    public CategoryDto.Response getCategoryBySlug(String slug, String tenantId) {
        log.debug("Fetching category by slug: {} for tenant: {}", slug, tenantId);
        Category category = categoryRepository.findBySlugAndTenantId(slug, tenantId)
            .filter(c -> !c.getIsDeleted())
            .orElseThrow(() -> new RuntimeException("Category not found: " + slug));
        return categoryMapper.toResponse(category);
    }

    public Page<CategoryDto.Response> getAllCategories(String tenantId, Pageable pageable) {
        return categoryRepository.findAllActive(tenantId, pageable)
            .map(categoryMapper::toResponse);
    }

    public Page<CategoryDto.Response> getDeletedCategories(String tenantId, Pageable pageable) {
        return categoryRepository.findAllDeleted(tenantId, pageable)
            .map(categoryMapper::toResponse);
    }

    public List<CategoryDto.Response> getSubcategories(String parentId) {
        return categoryRepository.findSubcategories(parentId)
            .stream()
            .map(categoryMapper::toResponse)
            .collect(Collectors.toList());
    }

    public List<CategoryDto.Response> getMenuCategories(String tenantId) {
        return categoryRepository.findMenuCategories(tenantId)
            .stream()
            .map(categoryMapper::toResponse)
            .collect(Collectors.toList());
    }

    public List<CategoryDto.Response> getCategoriesByLevel(Integer level, String tenantId) {
        return categoryRepository.findByHierarchyLevel(level, tenantId)
            .stream()
            .map(categoryMapper::toResponse)
            .collect(Collectors.toList());
    }

    // === WRITE OPERATIONS ===

    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public CategoryDto.Response createCategory(CategoryDto.Request request, String currentUserId, String tenantId) {
        log.info("Creating category: {} by user: {}", request.getName(), currentUserId);

        validateSlugUniqueness(request.getSlug(), null, tenantId);

        Category category = categoryMapper.toEntity(request);
        category.setId(UUID.randomUUID().toString());
        category.setTenantId(tenantId);
        category.setCreatedBy(currentUserId);
        category.setUpdatedBy(currentUserId);

        applyCustomFields(category, request.getCustomFields());

        // Set hierarchy fields
        if (StringUtils.hasText(request.getParentId())) {
            Category parent = categoryRepository.findById(request.getParentId())
                .orElseThrow(() -> new RuntimeException("Parent category not found: " + request.getParentId()));
            
            validateSameTenant(parent, tenantId);
            validateNotCircular(category, parent);
            
            category.setParent(parent);
            category.setHierarchyLevel(parent.getHierarchyLevel() + 1);
            category.setMaterializedPath(parent.getMaterializedPath() + "/" + category.getId());
        } else {
            category.setHierarchyLevel(0);
            category.setMaterializedPath("/" + category.getId());
        }

        Category saved = categoryRepository.save(category);
        
        // Reorder if sortOrder specified
        if (request.getSortOrder() != null) {
            reorderSiblings(saved);
        }
        
        log.info("Created category with id: {}", saved.getId());
        return categoryMapper.toResponse(saved);
    }

    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public CategoryDto.Response updateCategory(String id, CategoryDto.Request request, String currentUserId) {
        log.info("Updating category: {} by user: {}", id, currentUserId);

        Category category = categoryRepository.findByIdWithFullHierarchy(id)
            .orElseThrow(() -> new RuntimeException("Category not found: " + id));

        if (!category.getSlug().equals(request.getSlug())) {
            validateSlugUniqueness(request.getSlug(), id, category.getTenantId());
        }

        categoryMapper.updateEntityFromDto(request, category);
        category.setUpdatedBy(currentUserId);

        applyCustomFields(category, request.getCustomFields());

        // Handle parent change with hierarchy update
        handleParentChange(category, request.getParentId(), currentUserId);

        Category updated = categoryRepository.save(category);
        return categoryMapper.toResponse(updated);
    }

    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public void deleteCategory(String id, String currentUserId) {
        log.info("Soft deleting category: {} by user: {}", id, currentUserId);
        
        categoryRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Category not found: " + id));
        
        // Check if has subcategories
        List<Category> subcategories = categoryRepository.findSubcategories(id);
        if (!subcategories.isEmpty()) {
            throw new RuntimeException("Cannot delete category with subcategories. Move or delete subcategories first.");
        }

        int updated = categoryRepository.softDelete(id, Instant.now(), currentUserId);
        if (updated == 0) {
            throw new RuntimeException("Failed to delete category");
        }
    }

    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public CategoryDto.Response restoreCategory(String id, String currentUserId) {
        log.info("Restoring category: {} by user: {}", id, currentUserId);
        
        int updated = categoryRepository.restore(id);
        if (updated == 0) {
            throw new RuntimeException("Category not found or not deleted");
        }

        return categoryRepository.findById(id)
            .map(categoryMapper::toResponse)
            .orElseThrow(() -> new RuntimeException("Restored category not found"));
    }

    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public CategoryDto.Response moveCategory(String id, String newParentId, String currentUserId) {
        log.info("Moving category {} to parent {} by user {}", id, newParentId, currentUserId);

        Category category = categoryRepository.findByIdWithFullHierarchy(id)
            .orElseThrow(() -> new RuntimeException("Category not found: " + id));

        if (StringUtils.hasText(newParentId) && newParentId.equals(id)) {
            throw new RuntimeException("Cannot move category to itself");
        }

        Category newParent = StringUtils.hasText(newParentId) 
            ? categoryRepository.findById(newParentId)
                .orElseThrow(() -> new RuntimeException("Parent not found: " + newParentId))
            : null;

        validateSameTenant(newParent, category.getTenantId());
        validateNotCircular(category, newParent);

        // Calculate path changes
        String oldPathPrefix = category.getMaterializedPath();
        int oldLevel = category.getHierarchyLevel();
        
        // Update parent relationship
        category.setParent(newParent);
        
        // Recalculate hierarchy fields
        if (newParent != null) {
            category.setHierarchyLevel(newParent.getHierarchyLevel() + 1);
            category.setMaterializedPath(newParent.getMaterializedPath() + "/" + category.getId());
        } else {
            category.setHierarchyLevel(0);
            category.setMaterializedPath("/" + category.getId());
        }
        
        category.setUpdatedBy(currentUserId);
        
        int newLevel = category.getHierarchyLevel();
        int levelDelta = newLevel - oldLevel;
        String newPathPrefix = category.getMaterializedPath();

        Category saved = categoryRepository.save(category);
        
        // Update all descendants' paths
        if (!oldPathPrefix.equals(newPathPrefix)) {
            categoryRepository.updateSubtreePaths(oldPathPrefix, newPathPrefix, levelDelta);
        }
        
        return categoryMapper.toResponse(saved);
    }

    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public void reorderCategory(String id, int newSortOrder, String currentUserId) {
        log.info("Reordering category {} to position {} by user {}", id, newSortOrder, currentUserId);
        
        Category category = categoryRepository.findByIdWithParent(id)
            .orElseThrow(() -> new RuntimeException("Category not found: " + id));
        
        String parentId = category.getParent() != null ? category.getParent().getId() : null;
        int currentOrder = category.getSortOrder();
        
        if (currentOrder == newSortOrder) {
            return;
        }

        // Shift other categories
        if (newSortOrder > currentOrder) {
            // Moving down: shift categories between current+1 and new up by 1
            categoryRepository.shiftSortOrder(parentId, currentOrder + 1, newSortOrder + 1);
        } else {
            // Moving up: shift categories between new and current-1 down by 1
            categoryRepository.shiftSortOrder(parentId, newSortOrder, currentOrder);
        }
        
        category.setSortOrder(newSortOrder);
        category.setUpdatedBy(currentUserId);
        categoryRepository.save(category);
    }

    @Transactional
    public void updateCustomFields(String id, Map<String, Object> fields, String currentUserId) {
        log.info("Updating custom fields for category: {} by user {}", id, currentUserId);
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Category not found: " + id));

        category.getCustomFields().clear();

        if (!CollectionUtils.isEmpty(fields)) {
            fields.forEach((key, value) -> {
                if (value != null) {
                    category.getCustomFields().put(key, value.toString());
                }
            });
        }
        
        category.setUpdatedBy(currentUserId);
        categoryRepository.save(category);
    }

    public Map<String, Object> getCustomFields(String id) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Category not found: " + id));
        return category.getCustomFields();
    }

    @Transactional
    public void toggleActive(String id, boolean active, String currentUserId) {
        int updated = categoryRepository.updateActiveStatus(id, active, Instant.now(), currentUserId);
        if (updated == 0) {
            throw new RuntimeException("Category not found");
        }
    }

    // === HIERARCHY QUERIES ===

    public List<CategoryDto.Response> getCategoryPath(String id) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Category not found: " + id));
        
        List<Category> path = new ArrayList<>();
        Category current = category;
        while (current != null) {
            path.add(0, current);
            current = current.getParent();
        }
        
        return path.stream()
            .map(categoryMapper::toResponse)
            .collect(Collectors.toList());
    }

    public List<CategoryDto.Response> getCategorySubtree(String id) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Category not found: " + id));
        
        return categoryRepository.findSubtree(category.getMaterializedPath())
            .stream()
            .map(categoryMapper::toResponse)
            .collect(Collectors.toList());
    }

    public List<String> getAllSubcategoryIds(String id) {
        return categoryRepository.findAllDescendantIds(id);
    }

    public long countDescendants(String id) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Category not found: " + id));
        return categoryRepository.countDescendants(category.getMaterializedPath(), id);
    }

    // === STATISTICS ===

    public Map<String, Long> getStatistics(String tenantId) {
        long total = categoryRepository.countActive(tenantId);
        List<Object[]> byLevel = categoryRepository.countByHierarchyLevel(tenantId);
        
        Map<String, Long> stats = new java.util.HashMap<>();
        stats.put("total", total);
        byLevel.forEach(row -> stats.put("level_" + row[0], (Long) row[1]));
        
        return stats;
    }

    // === PRIVATE HELPERS ===

    private void validateSlugUniqueness(String slug, String excludeId, String tenantId) {
        categoryRepository.findBySlugAndTenantId(slug, tenantId).ifPresent(existing -> {
            if (excludeId == null || !existing.getId().equals(excludeId)) {
                throw new RuntimeException("Slug already exists in tenant: " + slug);
            }
        });
    }

    private void applyCustomFields(Category category, Map<String, Object> fields) {
        if (CollectionUtils.isEmpty(fields)) return;
        fields.forEach((key, value) -> {
            if (value != null) {
                category.getCustomFields().put(key, value.toString());
            }
        });
    }

    private void handleParentChange(Category category, String newParentId, String currentUserId) {
        String currentParentId = category.getParent() != null ? category.getParent().getId() : null;

        if (Objects.equals(newParentId, currentParentId)) {
            return;
        }

        Category newParent = StringUtils.hasText(newParentId)
            ? categoryRepository.findById(newParentId)
                .orElseThrow(() -> new RuntimeException("Parent not found: " + newParentId))
            : null;

        validateSameTenant(newParent, category.getTenantId());
        validateNotCircular(category, newParent);

        // Calculate path changes
        String oldPath = category.getMaterializedPath();
        int oldLevel = category.getHierarchyLevel();

        category.setParent(newParent);
        
        if (newParent != null) {
            category.setHierarchyLevel(newParent.getHierarchyLevel() + 1);
            category.setMaterializedPath(newParent.getMaterializedPath() + "/" + category.getId());
        } else {
            category.setHierarchyLevel(0);
            category.setMaterializedPath("/" + category.getId());
        }

        // Update descendants
        int levelDelta = category.getHierarchyLevel() - oldLevel;
        if (!oldPath.equals(category.getMaterializedPath())) {
            categoryRepository.updateSubtreePaths(oldPath, category.getMaterializedPath(), levelDelta);
        }
        
        category.setUpdatedBy(currentUserId);
    }

    private void validateNotCircular(Category category, Category newParent) {
        if (newParent == null) return;

        Category current = newParent;
        while (current != null) {
            if (current.getId().equals(category.getId())) {
                throw new RuntimeException("Circular reference detected");
            }
            current = current.getParent();
        }
    }

    private void validateSameTenant(Category parent, String tenantId) {
        if (parent != null && !Objects.equals(parent.getTenantId(), tenantId)) {
            throw new RuntimeException("Cannot assign parent from different tenant");
        }
    }

    private void reorderSiblings(Category category) {
        String parentId = category.getParent() != null ? category.getParent().getId() : null;
        categoryRepository.shiftSortOrder(parentId, category.getSortOrder(), 1);
    }
}