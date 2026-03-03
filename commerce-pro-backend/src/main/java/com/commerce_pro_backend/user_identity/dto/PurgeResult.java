// PurgeResult.java
package com.commerce_pro_backend.user_identity.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PurgeResult {
    private long deletedCount;
    private LocalDateTime purgedBefore;
    private boolean dryRun;
    private String backupLocation;
}