// ExportRequest.java
package com.commerce_pro_backend.user_identity.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ExportRequest {
    private LocalDateTime from;
    private LocalDateTime to;
    private List<String> actions;
    private String actorId;
    private String format; // CSV, JSON, PDF
}
