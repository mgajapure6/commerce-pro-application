// AuditLogDTO.java
package com.commerce_pro_backend.user_identity.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AuditLogDTO {
    private String id;
    private String action;
    private String actorId;
    private String actorUsername;
    private String targetType;
    private String targetId;
    private String targetIdentifier;
    private String description;
    private boolean success;
    private String failureReason;
    private String ipAddress;
    private LocalDateTime timestamp;
    private Long processingTimeMs;
}