// AuditLogRepository.java
package com.commerce_pro_backend.user_identity.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.commerce_pro_backend.user_identity.entity.AuditLog;
import com.commerce_pro_backend.user_identity.enums.AuditAction;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, String> {

    @Query("SELECT a FROM AuditLog a WHERE " +
           "(:actorId IS NULL OR a.actorId = :actorId) AND " +
           "(:targetId IS NULL OR a.targetId = :targetId) AND " +
           "(:action IS NULL OR a.action = :action) AND " +
           "(:from IS NULL OR a.timestamp >= :from) AND " +
           "(:to IS NULL OR a.timestamp <= :to) AND " +
           "(:search IS NULL OR LOWER(a.actionDescription) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<AuditLog> searchLogs(@Param("actorId") String actorId,
                              @Param("targetId") String targetId,
                              @Param("action") AuditAction action,
                              @Param("from") LocalDateTime from,
                              @Param("to") LocalDateTime to,
                              @Param("search") String search,
                              Pageable pageable);
    
    @Query("SELECT a.action, COUNT(a) FROM AuditLog a " +
           "WHERE a.timestamp BETWEEN :from AND :to GROUP BY a.action")
    List<Object[]> countByActionBetween(@Param("from") LocalDateTime from, 
                                        @Param("to") LocalDateTime to);
    
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.timestamp < :before")
    long countOlderThan(@Param("before") LocalDateTime before);
    
    @Modifying
    @Query("DELETE FROM AuditLog a WHERE a.timestamp < :before")
    int deleteOlderThan(@Param("before") LocalDateTime before);
}