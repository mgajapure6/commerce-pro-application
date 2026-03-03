package com.commerce_pro_backend.catalog.category.mapper;

import com.commerce_pro_backend.catalog.category.dto.CategoryDto;
import com.commerce_pro_backend.catalog.category.entity.Category;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CategoryMapper {

    // === ENTITY CREATION ===

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "subcategories", ignore = true)
    @Mapping(target = "hierarchyLevel", ignore = true)
    @Mapping(target = "materializedPath", ignore = true)
    @Mapping(target = "isDeleted", constant = "false")
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "externalRef", ignore = true)
    @Mapping(target = "customFields", ignore = true)
    Category toEntity(CategoryDto.Request dto);

    // === RESPONSE MAPPING ===

    @Mapping(target = "parentId", source = "parent.id")
    @Mapping(target = "parentName", source = "parent.name")
    @Mapping(target = "subcategories", source = "subcategories")
    CategoryDto.Response toResponse(Category entity);

    List<CategoryDto.Response> toResponseList(List<Category> entities);

    // === CHILD RESPONSE ===

    @Mapping(target = "hasChildren", expression = "java(entity.getSubcategories() != null && !entity.getSubcategories().isEmpty())")
    @Mapping(target = "imageUrl", source = "imageUrl")
    @Mapping(target = "hierarchyLevel", source = "hierarchyLevel")
    CategoryDto.ChildResponse toChildResponse(Category entity);

    List<CategoryDto.ChildResponse> toChildResponseList(List<Category> entities);

    // === TREE RESPONSE ===

    @Mapping(target = "children", source = "subcategories")
    @Mapping(target = "imageUrl", source = "imageUrl")
    @Mapping(target = "hierarchyLevel", source = "hierarchyLevel")
    CategoryDto.TreeResponse toTreeResponse(Category entity);

    List<CategoryDto.TreeResponse> toTreeResponseList(List<Category> entities);

    // === BREADCRUMB RESPONSE ===

    @Mapping(target = "hierarchyLevel", source = "hierarchyLevel")
    CategoryDto.BreadcrumbResponse toBreadcrumbResponse(Category entity);

    List<CategoryDto.BreadcrumbResponse> toBreadcrumbResponseList(List<Category> entities);

    // === UPDATE MAPPING ===

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "subcategories", ignore = true)
    @Mapping(target = "hierarchyLevel", ignore = true)
    @Mapping(target = "materializedPath", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "externalRef", ignore = true)
    @Mapping(target = "customFields", ignore = true)
    void updateEntityFromDto(CategoryDto.Request dto, @MappingTarget Category entity);
}