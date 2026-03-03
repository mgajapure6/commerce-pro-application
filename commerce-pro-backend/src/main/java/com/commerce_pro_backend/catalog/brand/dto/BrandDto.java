package com.commerce_pro_backend.catalog.brand.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

public class BrandDto {

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

        @Size(max = 500)
        private String logo;

        @Pattern(regexp = "^(https?://)?([\\da-z.-]+)\\.([a-z.]{2,6})([/\\w .-]*)*/?$", message = "Invalid website URL")
        @Size(max = 500)
        private String website;

        private Boolean isActive;
        private Boolean isFeatured;
        private Integer sortOrder;
    }

    @Data
    @Builder
    public static class Response {
        private String id;
        private String name;
        private String slug;
        private String description;
        private String logo;
        private String website;
        private Boolean isActive;
        private Boolean isFeatured;
        private Integer productCount;
        private Integer sortOrder;
        private Instant createdAt;
        private Instant updatedAt;
    }

    @Data
    @Builder
    public static class ListResponse {
        private String id;
        private String name;
        private String slug;
        private String logo;
        private Boolean isActive;
        private Boolean isFeatured;
        private Integer productCount;
    }
}