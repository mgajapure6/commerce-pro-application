package com.commerce_pro_backend.catalog.brand.repository;

import com.commerce_pro_backend.catalog.brand.entity.Brand;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BrandRepository extends JpaRepository<Brand, String>, JpaSpecificationExecutor<Brand> {

    Optional<Brand> findBySlug(String slug);

    boolean existsBySlug(String slug);

    List<Brand> findByIsActiveTrueOrderBySortOrderAsc();

    List<Brand> findByIsFeaturedTrueAndIsActiveTrueOrderBySortOrderAsc();

    Page<Brand> findByIsActiveTrueOrderBySortOrderAsc(Pageable pageable);

    @Modifying
    @Query("UPDATE Brand b SET b.productCount = b.productCount + :delta WHERE b.id = :id")
    int updateProductCount(@Param("id") String id, @Param("delta") int delta);

    @Modifying
    @Query("UPDATE Brand b SET b.isActive = :active WHERE b.id = :id")
    int updateActiveStatus(@Param("id") String id, @Param("active") boolean active);

    @Modifying
    @Query("UPDATE Brand b SET b.isFeatured = :featured WHERE b.id = :id")
    int updateFeaturedStatus(@Param("id") String id, @Param("featured") boolean featured);
}