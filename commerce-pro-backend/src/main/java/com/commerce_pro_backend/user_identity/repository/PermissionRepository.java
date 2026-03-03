// PermissionRepository.java
package com.commerce_pro_backend.user_identity.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.commerce_pro_backend.user_identity.entity.Permission;
import com.commerce_pro_backend.user_identity.enums.PermissionCategory;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, String> {

    @Query("SELECT p FROM Permission p WHERE " +
           "(:category IS NULL OR p.category = :category) AND " +
           "(:systemOnly IS NULL OR p.isSystem = :systemOnly)")
    Page<Permission> findWithFilters(@Param("category") PermissionCategory category,
                                     @Param("systemOnly") Boolean systemOnly,
                                     Pageable pageable);
    
    List<Permission> findByCategory(PermissionCategory category);
    
    @Query("SELECT COUNT(r) FROM Role r JOIN r.permissions p WHERE p.code = :permissionCode")
    long countRolesUsingPermission(@Param("permissionCode") String permissionCode);
}