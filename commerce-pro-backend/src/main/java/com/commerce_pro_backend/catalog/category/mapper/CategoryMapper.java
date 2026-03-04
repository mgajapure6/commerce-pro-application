package com.commerce_pro_backend.catalog.category.mapper;

import com.commerce_pro_backend.catalog.category.dto.CategoryDto;
import com.commerce_pro_backend.catalog.category.entity.Category;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class CategoryMapper {

    public Category toEntity(CategoryDto.Request dto) {
        if (dto == null) {
            return null;
        }

        return Category.builder()
                .name(dto.getName())
                .slug(dto.getSlug())
                .description(dto.getDescription())
                .imageUrl(dto.getImageUrl())
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : true)
                .sortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0)
                .showInMenu(dto.getShowInMenu() != null ? dto.getShowInMenu() : true)
                .seoTitle(dto.getSeoTitle())
                .seoDescription(dto.getSeoDescription())
                .metaKeywords(dto.getMetaKeywords())
                .customFields(dto.getCustomFields() != null ? new HashMap<>(dto.getCustomFields()) : new HashMap<>())
                .build();
    }

    public CategoryDto.Response toResponse(Category entity) {
        if (entity == null) {
            return null;
        }

        return CategoryDto.Response.builder()
                .id(entity.getId())
                .name(entity.getName())
                .slug(entity.getSlug())
                .description(entity.getDescription())
                .parentId(entity.getParent() != null ? entity.getParent().getId() : null)
                .parentName(entity.getParent() != null ? entity.getParent().getName() : null)
                .hierarchyLevel(entity.getHierarchyLevel())
                .materializedPath(entity.getMaterializedPath())
                .subcategories(toChildResponseList(entity.getSubcategories()))
                .imageUrl(entity.getImageUrl())
                .isActive(entity.getIsActive())
                .sortOrder(entity.getSortOrder())
                .showInMenu(entity.getShowInMenu())
                .seoTitle(entity.getSeoTitle())
                .seoDescription(entity.getSeoDescription())
                .metaKeywords(entity.getMetaKeywords())
                .customFields(entity.getCustomFields())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .isDeleted(entity.getIsDeleted())
                .deletedAt(entity.getDeletedAt())
                .deletedBy(entity.getDeletedBy())
                .tenantId(entity.getTenantId())
                .externalRef(entity.getExternalRef())
                .build();
    }

    public List<CategoryDto.Response> toResponseList(List<Category> entities) {
        if (entities == null) {
            return List.of();
        }

        return entities.stream()
                .map(this::toResponse)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public CategoryDto.ChildResponse toChildResponse(Category entity) {
        if (entity == null) {
            return null;
        }

        return CategoryDto.ChildResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .slug(entity.getSlug())
                .imageUrl(entity.getImageUrl())
                .hierarchyLevel(entity.getHierarchyLevel())
                .hasChildren(entity.getSubcategories() != null && !entity.getSubcategories().isEmpty())
                .build();
    }

    public List<CategoryDto.ChildResponse> toChildResponseList(List<Category> entities) {
        if (entities == null) {
            return List.of();
        }

        return entities.stream()
                .map(this::toChildResponse)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public CategoryDto.TreeResponse toTreeResponse(Category entity) {
        if (entity == null) {
            return null;
        }

        return CategoryDto.TreeResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .slug(entity.getSlug())
                .imageUrl(entity.getImageUrl())
                .hierarchyLevel(entity.getHierarchyLevel())
                .children(toTreeResponseList(entity.getSubcategories()))
                .build();
    }

    public List<CategoryDto.TreeResponse> toTreeResponseList(List<Category> entities) {
        if (entities == null) {
            return List.of();
        }

        return entities.stream()
                .map(this::toTreeResponse)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public CategoryDto.BreadcrumbResponse toBreadcrumbResponse(Category entity) {
        if (entity == null) {
            return null;
        }

        return CategoryDto.BreadcrumbResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .slug(entity.getSlug())
                .hierarchyLevel(entity.getHierarchyLevel())
                .build();
    }

    public List<CategoryDto.BreadcrumbResponse> toBreadcrumbResponseList(List<Category> entities) {
        if (entities == null) {
            return List.of();
        }

        return entities.stream()
                .map(this::toBreadcrumbResponse)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public void updateEntityFromDto(CategoryDto.Request dto, Category entity) {
        if (dto == null || entity == null) {
            return;
        }

        if (dto.getName() != null) {
            entity.setName(dto.getName());
        }
        if (dto.getSlug() != null) {
            entity.setSlug(dto.getSlug());
        }
        if (dto.getDescription() != null) {
            entity.setDescription(dto.getDescription());
        }
        if (dto.getImageUrl() != null) {
            entity.setImageUrl(dto.getImageUrl());
        }
        if (dto.getIsActive() != null) {
            entity.setIsActive(dto.getIsActive());
        }
        if (dto.getSortOrder() != null) {
            entity.setSortOrder(dto.getSortOrder());
        }
        if (dto.getShowInMenu() != null) {
            entity.setShowInMenu(dto.getShowInMenu());
        }
        if (dto.getSeoTitle() != null) {
            entity.setSeoTitle(dto.getSeoTitle());
        }
        if (dto.getSeoDescription() != null) {
            entity.setSeoDescription(dto.getSeoDescription());
        }
        if (dto.getMetaKeywords() != null) {
            entity.setMetaKeywords(dto.getMetaKeywords());
        }
        if (dto.getCustomFields() != null) {
            entity.setCustomFields(new HashMap<>(dto.getCustomFields()));
        }
    }
}
