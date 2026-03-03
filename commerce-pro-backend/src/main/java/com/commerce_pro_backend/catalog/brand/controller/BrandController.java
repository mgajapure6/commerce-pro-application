package com.commerce_pro_backend.catalog.brand.controller;

import com.commerce_pro_backend.catalog.brand.dto.BrandDto;
import com.commerce_pro_backend.catalog.brand.service.BrandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/brands")
@RequiredArgsConstructor
public class BrandController {

    private final BrandService brandService;

    @GetMapping
    public ResponseEntity<Page<BrandDto.ListResponse>> getAll(Pageable pageable) {
        return ResponseEntity.ok(brandService.getAllBrands(pageable));
    }

    @GetMapping("/all")
    public ResponseEntity<List<BrandDto.ListResponse>> getAllActive() {
        return ResponseEntity.ok(brandService.getAllActiveBrands());
    }

    @GetMapping("/featured")
    public ResponseEntity<List<BrandDto.ListResponse>> getFeatured() {
        return ResponseEntity.ok(brandService.getFeaturedBrands());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BrandDto.Response> getById(@PathVariable String id) {
        return ResponseEntity.ok(brandService.getBrand(id));
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<BrandDto.Response> getBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(brandService.getBrandBySlug(slug));
    }

    @PostMapping
    public ResponseEntity<BrandDto.Response> create(
            @Valid @RequestBody BrandDto.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(brandService.createBrand(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BrandDto.Response> update(
            @PathVariable String id,
            @Valid @RequestBody BrandDto.Request request) {
        return ResponseEntity.ok(brandService.updateBrand(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        brandService.deleteBrand(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/active")
    public ResponseEntity<Void> toggleActive(
            @PathVariable String id,
            @RequestParam boolean active) {
        brandService.toggleActive(id, active);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/featured")
    public ResponseEntity<Void> toggleFeatured(
            @PathVariable String id,
            @RequestParam boolean featured) {
        brandService.toggleFeatured(id, featured);
        return ResponseEntity.ok().build();
    }
}