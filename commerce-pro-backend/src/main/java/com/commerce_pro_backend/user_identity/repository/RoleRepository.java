// RoleRepository.java
package com.commerce_pro_backend.user_identity.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.commerce_pro_backend.user_identity.entity.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, String> {

    Optional<Role> findByCode(String code);
    
    boolean existsByCode(String code);
    
    @EntityGraph(attributePaths = {"permissions", "parentRole"})
    Optional<Role> findWithDetailsById(String id);
    
    @Query("SELECT r FROM Role r WHERE :systemOnly IS NULL OR r.isSystem = :systemOnly")
    Page<Role> findWithFilter(@Param("systemOnly") Boolean systemOnly, Pageable pageable);
    
    @Query("SELECT r FROM Role r WHERE r.parentRole IS NULL")
    List<Role> findRootRoles();
    
    @Query("SELECT COUNT(ra) FROM UserRoleAssignment ra WHERE ra.role.id = :roleId AND ra.status = 'ACTIVE'")
    long countActiveAssignments(@Param("roleId") String roleId);
}