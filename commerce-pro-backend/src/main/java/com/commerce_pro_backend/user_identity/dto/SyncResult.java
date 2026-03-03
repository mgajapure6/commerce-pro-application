// SyncResult.java
package com.commerce_pro_backend.user_identity.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SyncResult {
    private int created;
    private int updated;
    private int unchanged;
    private int failed;
    private long executionTimeMs;
}