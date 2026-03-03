package com.commerce_pro_backend.catalog.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public class CategoryDto {

    @Data
    @Builder
    public static class Request {
        @NotBlank
        @Size(max = 100)
        private String name;

        @NotBlank
        @Size(max = 150)
        @Pattern(regexp = "^[a-z0-9-]+$", message = "Slug must be lowercase alphanumeric with hyphens")
        private String slug;

        @Size(max = 2000)
        private String description;

        private String parentId;

        @Size(max = 500)
        private String imageUrl;

        private Boolean isActive;

        private Integer sortOrder;

        private Boolean showInMenu;

        @Size(max = 70)
        private String seoTitle;

        @Size(max = 160)
        private String seoDescription;

        @Size(max = 500)
        private String metaKeywords;

        private Map<String, Object> customFields;

        // Hierarchy fields (optional, usually auto-calculated)
        private Integer hierarchyLevel;
        private String materializedPath;
    }

    @Data
    @Builder
    public static class Response {
        private String id;
        private String name;
        private String slug;
        private String description;
        
        // Hierarchy
        private String parentId;
        private String parentName;
        private Integer hierarchyLevel;
        private String materializedPath;
        private List<ChildResponse> subcategories;
        
        // Display
        private String imageUrl;
        private Boolean isActive;
        private Integer sortOrder;
        private Boolean showInMenu;
        
        // SEO
        private String seoTitle;
        private String seoDescription;
        private String metaKeywords;
        
        // Extensibility
        private Map<String, Object> customFields;
        
        // Audit
        private Instant createdAt;
        private Instant updatedAt;
        private String createdBy;
        private String updatedBy;
        
        // Soft delete
        private Boolean isDeleted;
        private Instant deletedAt;
        private String deletedBy;
        
        // Multi-tenancy
        private String tenantId;
        
        // Integration
        private String externalRef;
    }

    @Data
    @Builder
    public static class ChildResponse {
        private String id;
        private String name;
        private String slug;
        private String imageUrl;
        private Boolean hasChildren;
        private Integer hierarchyLevel;
    }

    @Data
    @Builder
    public static class TreeResponse {
        private String id;
        private String name;
        private String slug;
        private String imageUrl;
        private Integer hierarchyLevel;
        private List<TreeResponse> children;
    }

    @Data
    @Builder
    public static class ListResponse {
        private List<Response> categories;
        private Long total;
        private Integer page;
        private Integer size;
    }

    @Data
    @Builder
    public static class CustomFieldUpdateRequest {
        private Map<String, Object> fields;
    }

    @Data
    @Builder
    public static class MoveRequest {
        private String newParentId;
    }

    @Data
    @Builder
    public static class ReorderRequest {
        private Integer newSortOrder;
    }

    @Data
    @Builder
    public static class BreadcrumbResponse {
        private String id;
        private String name;
        private String slug;
        private Integer hierarchyLevel;
    }

    @Data
    @Builder
    public static class StatisticsResponse {
        private Long totalCategories;
        private Map<String, Long> categoriesByLevel;
        private Long rootCategories;
        private Long activeCategories;
        private Long deletedCategories;
    }
}