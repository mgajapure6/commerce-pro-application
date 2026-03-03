package com.commerce_pro_backend.catalog.brand.service;

import com.commerce_pro_backend.catalog.brand.dto.BrandDto;
import com.commerce_pro_backend.catalog.brand.entity.Brand;
import com.commerce_pro_backend.catalog.brand.mapper.BrandMapper;
import com.commerce_pro_backend.catalog.brand.repository.BrandRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BrandService {

    private final BrandRepository brandRepository;
    private final BrandMapper brandMapper;

    @Cacheable(value = "brands", key = "'all-active'")
    public List<BrandDto.ListResponse> getAllActiveBrands() {
        return brandRepository.findByIsActiveTrueOrderBySortOrderAsc()
            .stream()
            .map(brandMapper::toListResponse)
            .collect(Collectors.toList());
    }

    @Cacheable(value = "brands", key = "'featured'")
    public List<BrandDto.ListResponse> getFeaturedBrands() {
        return brandRepository.findByIsFeaturedTrueAndIsActiveTrueOrderBySortOrderAsc()
            .stream()
            .map(brandMapper::toListResponse)
            .collect(Collectors.toList());
    }

    public Page<BrandDto.ListResponse> getAllBrands(Pageable pageable) {
        return brandRepository.findByIsActiveTrueOrderBySortOrderAsc(pageable)
            .map(brandMapper::toListResponse);
    }

    public BrandDto.Response getBrand(String id) {
        Brand brand = brandRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Brand not found: " + id));
        return brandMapper.toResponse(brand);
    }

    public BrandDto.Response getBrandBySlug(String slug) {
        Brand brand = brandRepository.findBySlug(slug)
            .orElseThrow(() -> new RuntimeException("Brand not found: " + slug));
        return brandMapper.toResponse(brand);
    }

    @Transactional
    @CacheEvict(value = "brands", allEntries = true)
    public BrandDto.Response createBrand(BrandDto.Request request) {
        log.info("Creating brand: {}", request.getName());

        validateSlugUniqueness(request.getSlug(), null);

        Brand brand = brandMapper.toEntity(request);
        brand.setId(UUID.randomUUID().toString());

        Brand saved = brandRepository.save(brand);
        log.info("Created brand with id: {}", saved.getId());
        return brandMapper.toResponse(saved);
    }

    @Transactional
    @CacheEvict(value = "brands", allEntries = true)
    public BrandDto.Response updateBrand(String id, BrandDto.Request request) {
        log.info("Updating brand: {}", id);

        Brand brand = brandRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Brand not found: " + id));

        if (!brand.getSlug().equals(request.getSlug())) {
            validateSlugUniqueness(request.getSlug(), id);
        }

        brandMapper.updateEntityFromDto(request, brand);

        Brand updated = brandRepository.save(brand);
        return brandMapper.toResponse(updated);
    }

    @Transactional
    @CacheEvict(value = "brands", allEntries = true)
    public void deleteBrand(String id) {
        log.info("Deleting brand: {}", id);
        Brand brand = brandRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Brand not found: " + id));
        brandRepository.delete(brand);
    }

    @Transactional
    public void updateProductCount(String id, int delta) {
        brandRepository.updateProductCount(id, delta);
    }

    @Transactional
    @CacheEvict(value = "brands", allEntries = true)
    public void toggleActive(String id, boolean active) {
        brandRepository.updateActiveStatus(id, active);
    }

    @Transactional
    @CacheEvict(value = "brands", allEntries = true)
    public void toggleFeatured(String id, boolean featured) {
        brandRepository.updateFeaturedStatus(id, featured);
    }

    private void validateSlugUniqueness(String slug, String excludeId) {
        brandRepository.findBySlug(slug).ifPresent(existing -> {
            if (excludeId == null || !existing.getId().equals(excludeId)) {
                throw new RuntimeException("Slug already exists: " + slug);
            }
        });
    }
}