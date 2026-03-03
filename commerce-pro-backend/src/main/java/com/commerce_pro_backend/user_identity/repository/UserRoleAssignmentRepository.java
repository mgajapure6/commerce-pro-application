// UserRoleAssignmentRepository.java
package com.commerce_pro_backend.user_identity.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.commerce_pro_backend.user_identity.entity.UserRoleAssignment;
import com.commerce_pro_backend.user_identity.enums.AssignmentStatus;

@Repository
public interface UserRoleAssignmentRepository extends JpaRepository<UserRoleAssignment, String> {

    List<UserRoleAssignment> findByUserIdAndStatus(String userId, AssignmentStatus status);
    
    @Query("SELECT ra FROM UserRoleAssignment ra JOIN FETCH ra.role JOIN FETCH ra.user " +
           "WHERE ra.user.id = :userId AND ra.status = 'ACTIVE'")
    List<UserRoleAssignment> findActiveByUserId(@Param("userId") String userId);
    
    @Modifying
    @Query("UPDATE UserRoleAssignment ra SET ra.status = 'EXPIRED' " +
           "WHERE ra.validUntil < :now AND ra.status = 'ACTIVE'")
    int expireAssignments(@Param("now") LocalDateTime now);
    
    @Query("SELECT COUNT(ra) FROM UserRoleAssignment ra WHERE ra.role.id = :roleId")
    long countByRoleId(@Param("roleId") String roleId);
}