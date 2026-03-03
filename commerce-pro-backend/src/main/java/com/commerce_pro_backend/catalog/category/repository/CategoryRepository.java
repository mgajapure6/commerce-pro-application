package com.commerce_pro_backend.catalog.category.repository;

import com.commerce_pro_backend.catalog.category.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, String>, JpaSpecificationExecutor<Category> {

    // === BASIC LOOKUPS ===

    Optional<Category> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndTenantId(String slug, String tenantId);

    Optional<Category> findBySlugAndTenantId(String slug, String tenantId);

    Optional<Category> findByExternalRef(String externalRef);

    // === HIERARCHY QUERIES ===

    @EntityGraph(attributePaths = {"subcategories"})
    @Query("SELECT c FROM Category c WHERE c.parent IS NULL AND c.isActive = true AND c.isDeleted = false AND (c.tenantId = :tenantId OR :tenantId IS NULL) ORDER BY c.sortOrder ASC")
    List<Category> findRootCategories(@Param("tenantId") String tenantId);

    @EntityGraph(attributePaths = {"subcategories"})
    @Query("SELECT c FROM Category c WHERE c.parent.id = :parentId AND c.isActive = true AND c.isDeleted = false ORDER BY c.sortOrder ASC")
    List<Category> findSubcategories(@Param("parentId") String parentId);

    @Query("SELECT c FROM Category c WHERE c.materializedPath LIKE CONCAT(:path, '%') AND c.isDeleted = false ORDER BY c.materializedPath")
    List<Category> findSubtree(@Param("path") String materializedPath);

    @Query("SELECT c FROM Category c WHERE c.hierarchyLevel = :level AND c.isActive = true AND c.isDeleted = false AND (c.tenantId = :tenantId OR :tenantId IS NULL)")
    List<Category> findByHierarchyLevel(@Param("level") Integer level, @Param("tenantId") String tenantId);

    // === MENU & DISPLAY QUERIES ===

    @Query("SELECT c FROM Category c WHERE c.showInMenu = true AND c.isActive = true AND c.isDeleted = false AND (c.tenantId = :tenantId OR :tenantId IS NULL) ORDER BY c.sortOrder ASC")
    List<Category> findMenuCategories(@Param("tenantId") String tenantId);

    // === ADMIN/QUERY METHODS ===

    @Query("SELECT c FROM Category c WHERE c.isDeleted = false AND (c.tenantId = :tenantId OR :tenantId IS NULL)")
    Page<Category> findAllActive(@Param("tenantId") String tenantId, Pageable pageable);

    @Query("SELECT c FROM Category c WHERE c.isDeleted = true AND (c.tenantId = :tenantId OR :tenantId IS NULL)")
    Page<Category> findAllDeleted(@Param("tenantId") String tenantId, Pageable pageable);

    // === AUDIT & SOFT DELETE ===

    @Modifying
    @Query("UPDATE Category c SET c.isDeleted = true, c.deletedAt = :deletedAt, c.deletedBy = :deletedBy, c.isActive = false WHERE c.id = :id")
    int softDelete(@Param("id") String id, @Param("deletedAt") Instant deletedAt, @Param("deletedBy") String deletedBy);

    @Modifying
    @Query("UPDATE Category c SET c.isDeleted = false, c.deletedAt = NULL, c.deletedBy = NULL WHERE c.id = :id")
    int restore(@Param("id") String id);

    @Modifying
    @Query("UPDATE Category c SET c.isActive = :active, c.updatedBy = :updatedBy, c.updatedAt = :updatedAt WHERE c.id = :id")
    int updateActiveStatus(@Param("id") String id, @Param("active") boolean active, @Param("updatedAt") Instant updatedAt, @Param("updatedBy") String updatedBy);

    // === HIERARCHY MAINTENANCE ===

    @Modifying
    @Query("UPDATE Category c SET c.hierarchyLevel = :level, c.materializedPath = :path WHERE c.id = :id")
    int updateHierarchyFields(@Param("id") String id, @Param("level") Integer level, @Param("path") String materializedPath);

    @Modifying
    @Query(value = "UPDATE categories SET materialized_path = REPLACE(materialized_path, :oldPrefix, :newPrefix), " +
                   "hierarchy_level = hierarchy_level + :levelDelta " +
                   "WHERE materialized_path LIKE CONCAT(:oldPrefix, '%')", nativeQuery = true)
    int updateSubtreePaths(@Param("oldPrefix") String oldPrefix, @Param("newPrefix") String newPrefix, @Param("levelDelta") int levelDelta);

    // === FETCH GRAPHS ===

    @EntityGraph(attributePaths = {"parent"})
    @Query("SELECT c FROM Category c WHERE c.id = :id AND c.isDeleted = false")
    Optional<Category> findByIdWithParent(@Param("id") String id);

    @EntityGraph(attributePaths = {"subcategories"})
    @Query("SELECT c FROM Category c WHERE c.id = :id AND c.isDeleted = false")
    Optional<Category> findByIdWithSubcategories(@Param("id") String id);

    @EntityGraph(attributePaths = {"parent", "subcategories"})
    @Query("SELECT c FROM Category c WHERE c.id = :id AND c.isDeleted = false")
    Optional<Category> findByIdWithFullHierarchy(@Param("id") String id);

    // === NATIVE QUERIES FOR TREE OPERATIONS ===

    @Query(value = """
        WITH RECURSIVE category_tree AS (
            SELECT id, materialized_path, hierarchy_level 
            FROM categories 
            WHERE id = :categoryId AND is_deleted = false
            UNION ALL
            SELECT c.id, c.materialized_path, c.hierarchy_level
            FROM categories c
            INNER JOIN category_tree ct ON c.parent_id = ct.id
            WHERE c.is_deleted = false
        )
        SELECT id FROM category_tree
        """, nativeQuery = true)
    List<String> findAllDescendantIds(@Param("categoryId") String categoryId);

    @Query(value = """
        WITH RECURSIVE category_path AS (
            SELECT id, parent_id, name, slug, hierarchy_level, materialized_path
            FROM categories 
            WHERE id = :categoryId AND is_deleted = false
            UNION ALL
            SELECT c.id, c.parent_id, c.name, c.slug, c.hierarchy_level, c.materialized_path
            FROM categories c
            INNER JOIN category_path cp ON c.id = cp.parent_id
            WHERE c.is_deleted = false
        )
        SELECT * FROM category_path ORDER BY hierarchy_level ASC
        """, nativeQuery = true)
    List<Object[]> findAncestorPath(@Param("categoryId") String categoryId);

    @Query(value = """
        SELECT COUNT(*) FROM categories 
        WHERE materialized_path LIKE CONCAT(:path, '%') 
        AND id != :excludeId 
        AND is_deleted = false
        """, nativeQuery = true)
    long countDescendants(@Param("path") String materializedPath, @Param("excludeId") String excludeId);

    // === BULK OPERATIONS ===

    @Modifying
    @Query("UPDATE Category c SET c.sortOrder = c.sortOrder + :delta WHERE c.parent.id = :parentId AND c.sortOrder >= :startOrder")
    int shiftSortOrder(@Param("parentId") String parentId, @Param("startOrder") int startOrder, @Param("delta") int delta);

    // === STATISTICS ===

    @Query("SELECT COUNT(c) FROM Category c WHERE c.isDeleted = false AND (c.tenantId = :tenantId OR :tenantId IS NULL)")
    long countActive(@Param("tenantId") String tenantId);

    @Query("SELECT c.hierarchyLevel, COUNT(c) FROM Category c WHERE c.isDeleted = false AND (c.tenantId = :tenantId OR :tenantId IS NULL) GROUP BY c.hierarchyLevel")
    List<Object[]> countByHierarchyLevel(@Param("tenantId") String tenantId);
}