package com.commerce_pro_backend.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Warehouse DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseDTO {
    private String id;
    private String name;
    private String code;
    private String address;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    private String managerName;
    private String managerEmail;
    private String managerPhone;
    private Boolean isActive;
    private Boolean isDefault;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
