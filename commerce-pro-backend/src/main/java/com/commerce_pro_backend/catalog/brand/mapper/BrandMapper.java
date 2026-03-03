package com.commerce_pro_backend.catalog.brand.mapper;

import com.commerce_pro_backend.catalog.brand.dto.BrandDto;
import com.commerce_pro_backend.catalog.brand.entity.Brand;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BrandMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "productCount", constant = "0")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    Brand toEntity(BrandDto.Request dto);

    BrandDto.Response toResponse(Brand entity);

    @Mapping(target = "productCount", source = "productCount")
    BrandDto.ListResponse toListResponse(Brand entity);

    List<BrandDto.ListResponse> toListResponseList(List<Brand> entities);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "productCount", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntityFromDto(BrandDto.Request dto, @MappingTarget Brand entity);
}