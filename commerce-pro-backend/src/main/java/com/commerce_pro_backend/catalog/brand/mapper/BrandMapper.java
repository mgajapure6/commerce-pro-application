package com.commerce_pro_backend.catalog.brand.mapper;

import com.commerce_pro_backend.catalog.brand.dto.BrandDto;
import com.commerce_pro_backend.catalog.brand.entity.Brand;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class BrandMapper {

    public Brand toEntity(BrandDto.Request dto) {
        if (dto == null) {
            return null;
        }

        return Brand.builder()
                .name(dto.getName())
                .slug(dto.getSlug())
                .description(dto.getDescription())
                .logo(dto.getLogo())
                .website(dto.getWebsite())
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : true)
                .isFeatured(dto.getIsFeatured() != null ? dto.getIsFeatured() : false)
                .productCount(0)
                .sortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0)
                .build();
    }

    public BrandDto.Response toResponse(Brand entity) {
        if (entity == null) {
            return null;
        }

        return BrandDto.Response.builder()
                .id(entity.getId())
                .name(entity.getName())
                .slug(entity.getSlug())
                .description(entity.getDescription())
                .logo(entity.getLogo())
                .website(entity.getWebsite())
                .isActive(entity.getIsActive())
                .isFeatured(entity.getIsFeatured())
                .productCount(entity.getProductCount())
                .sortOrder(entity.getSortOrder())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public BrandDto.ListResponse toListResponse(Brand entity) {
        if (entity == null) {
            return null;
        }

        return BrandDto.ListResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .slug(entity.getSlug())
                .logo(entity.getLogo())
                .isActive(entity.getIsActive())
                .isFeatured(entity.getIsFeatured())
                .productCount(entity.getProductCount())
                .build();
    }

    public List<BrandDto.ListResponse> toListResponseList(List<Brand> entities) {
        if (entities == null) {
            return List.of();
        }

        return entities.stream()
                .map(this::toListResponse)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public void updateEntityFromDto(BrandDto.Request dto, Brand entity) {
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
        if (dto.getLogo() != null) {
            entity.setLogo(dto.getLogo());
        }
        if (dto.getWebsite() != null) {
            entity.setWebsite(dto.getWebsite());
        }
        if (dto.getIsActive() != null) {
            entity.setIsActive(dto.getIsActive());
        }
        if (dto.getIsFeatured() != null) {
            entity.setIsFeatured(dto.getIsFeatured());
        }
        if (dto.getSortOrder() != null) {
            entity.setSortOrder(dto.getSortOrder());
        }
    }
}
