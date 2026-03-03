package com.commerce_pro_backend.catalog.category.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import com.commerce_pro_backend.common.converter.CustomFieldsConverter;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "categories", indexes = {
    @Index(name = "idx_cat_slug", columnList = "slug", unique = true),
    @Index(name = "idx_cat_active", columnList = "is_active"),
    @Index(name = "idx_cat_parent", columnList = "parent_id"),
    @Index(name = "idx_cat_tenant", columnList = "tenant_id"),  // ADD
    @Index(name = "idx_cat_path", columnList = "materialized_path")  // ADD
})
@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Category {
    
    // === CORE FIELDS ===
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @NotBlank @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String name;
    
    @NotBlank @Size(max = 150)
    @Column(nullable = false, length = 150, unique = true)
    private String slug;
    
    @Size(max = 2000)
    private String description;
    
    // === HIERARCHY ===
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;
    
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    @OrderBy("sortOrder ASC")
    @Builder.Default
    private List<Category> subcategories = new ArrayList<>();
    
    @Column(name = "hierarchy_level")
    private Integer hierarchyLevel;
    
    @Column(name = "materialized_path", length = 500)
    private String materializedPath;
    
    // === DISPLAY ===
    @Size(max = 500)
    private String imageUrl;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;
    
    @Column(nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean showInMenu = true;
    
    // === SEO (Minimal) ===
    @Size(max = 70)
    private String seoTitle;
    
    @Size(max = 160)
    private String seoDescription;
    
    @Size(max = 500)
    private String metaKeywords;
    
    // === EXTENSIBILITY ===
    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(columnDefinition = "json")
    @Convert(converter = CustomFieldsConverter.class)
    @Builder.Default
    private Map<String, Object> customFields = new HashMap<>();
    
    // === AUDIT ===
    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;
    
    @UpdateTimestamp
    private Instant updatedAt;
    
    @Column(updatable = false, length = 36)
    private String createdBy;
    
    @Column(length = 36)
    private String updatedBy;
    
    // === SOFT DELETE ===
    @Builder.Default
    private Boolean isDeleted = false;
    
    private Instant deletedAt;

    @Column(length = 36)
    private String deletedBy;
    
    // === MULTI-TENANCY ===
    @Column(length = 36)
    private String tenantId;
    
    // === INTEGRATION ===
    @Column(length = 100)
    private String externalRef;
}