package com.commerce_pro_backend.catalog.product.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.commerce_pro_backend.catalog.product.entity.Product;

import java.util.List;
import java.util.Optional;

/**
 * Product Repository with JPA and Specifications support
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, String>, JpaSpecificationExecutor<Product> {

    // ============== FIND BY UNIQUE FIELDS ==============

    Optional<Product> findBySku(String sku);

    boolean existsBySku(String sku);

    boolean existsBySkuAndIdNot(String sku, String id);

    Optional<Product> findByUrlHandle(String urlHandle);

    // ============== STATUS & CATEGORY QUERIES ==============

    List<Product> findByStatus(String status);

    List<Product> findByStatusAndVisibility(String status, String visibility);

    List<Product> findByCategory(String category);

    Page<Product> findByCategory(String category, Pageable pageable);

    List<Product> findByFeaturedTrue();

    // ============== STOCK QUERIES ==============

    List<Product> findByStockStatus(String stockStatus);

    @Query("SELECT p FROM Product p WHERE p.stock <= p.lowStockThreshold AND p.stock > 0")
    List<Product> findLowStockProducts();

    @Query("SELECT p FROM Product p WHERE p.stockStatus = 'out_of_stock'")
    List<Product> findOutOfStockProducts();

    // ============== SEARCH QUERIES ==============

    @Query("SELECT p FROM Product p WHERE " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.sku) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.brand) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Product> search(@Param("query") String query, Pageable pageable);

    // ============== DASHBOARD QUERIES ==============

    @Query("SELECT p FROM Product p ORDER BY p.salesCount DESC")
    List<Product> findTopSelling(Pageable pageable);

    @Query("SELECT p FROM Product p ORDER BY p.revenue DESC")
    List<Product> findTopRevenue(Pageable pageable);

    // ============== STATISTICS QUERIES ==============

    @Query("SELECT COUNT(p) FROM Product p WHERE p.status = :status")
    long countByStatus(@Param("status") String status);

    @Query("SELECT p.status, COUNT(p) FROM Product p GROUP BY p.status")
    List<Object[]> countByStatusGrouped();

    @Query("SELECT SUM(p.revenue) FROM Product p")
    java.math.BigDecimal sumRevenue();

    @Query("SELECT SUM(p.salesCount) FROM Product p")
    Long sumSalesCount();

    // ============== BRAND & TAG QUERIES ==============

    @Query("SELECT DISTINCT p.brand FROM Product p ORDER BY p.brand")
    List<String> findAllBrands();

    @Query("SELECT DISTINCT t FROM Product p JOIN p.tags t ORDER BY t")
    List<String> findAllTags();

    @Query("SELECT p FROM Product p JOIN p.tags t WHERE t = :tag")
    Page<Product> findByTag(@Param("tag") String tag, Pageable pageable);
}
