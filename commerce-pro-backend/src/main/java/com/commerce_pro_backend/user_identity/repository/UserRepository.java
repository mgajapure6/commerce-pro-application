// UserRepository.java
package com.commerce_pro_backend.user_identity.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.commerce_pro_backend.user_identity.entity.User;
import com.commerce_pro_backend.user_identity.enums.AssignmentStatus;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    @Query("SELECT u FROM User u JOIN u.roleAssignments ra JOIN ra.role r " +
           "WHERE r.code = :roleCode AND ra.status = :status")
    Page<User> findByRoleCodeAndStatus(@Param("roleCode") String roleCode, 
                                        @Param("status") AssignmentStatus status, 
                                        Pageable pageable);
    
    @Query("SELECT COUNT(u) FROM User u JOIN u.roleAssignments ra JOIN ra.role r " +
           "WHERE r.code = :roleCode AND ra.status = :status")
    long countByRoleCodeAndStatus(@Param("roleCode") String roleCode, 
                                  @Param("status") AssignmentStatus status);
    
    @Query("SELECT u FROM User u WHERE " +
           "(:active IS NULL OR u.isActive = :active) AND " +
           "(:search IS NULL OR LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<User> findWithFilters(@Param("active") Boolean active,
                               @Param("search") String search,
                               Pageable pageable);
}