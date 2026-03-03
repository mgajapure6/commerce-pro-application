// ExportResult.java
package com.commerce_pro_backend.user_identity.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExportResult {
    private String downloadUrl;
    private String fileName;
    private long recordCount;
    private long fileSizeBytes;
    private String expiresAt;
}