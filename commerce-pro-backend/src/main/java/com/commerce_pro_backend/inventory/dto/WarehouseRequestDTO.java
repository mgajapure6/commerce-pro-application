package com.commerce_pro_backend.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Warehouse Request DTO - For creating/updating warehouses
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseRequestDTO {

    @NotBlank(message = "Warehouse name is required")
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
}
