package com.commerce_pro_backend.inventory.mapper;

import com.commerce_pro_backend.inventory.dto.*;
import com.commerce_pro_backend.inventory.entity.Inventory;
import com.commerce_pro_backend.inventory.entity.StockMovement;
import com.commerce_pro_backend.inventory.entity.Warehouse;
import com.commerce_pro_backend.product.dto.ProductSummaryDTO;
import com.commerce_pro_backend.product.entity.Product;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for Inventory entities and DTOs
 */
@Component
public class InventoryMapper {

    // ==================== Warehouse Mapping ====================

    public Warehouse toWarehouseEntity(WarehouseRequestDTO dto) {
        if (dto == null) return null;

        return Warehouse.builder()
                .name(dto.getName())
                .code(dto.getCode())
                .address(dto.getAddress())
                .city(dto.getCity())
                .state(dto.getState())
                .country(dto.getCountry())
                .postalCode(dto.getPostalCode())
                .managerName(dto.getManagerName())
                .managerEmail(dto.getManagerEmail())
                .managerPhone(dto.getManagerPhone())
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : true)
                .isDefault(dto.getIsDefault() != null ? dto.getIsDefault() : false)
                .build();
    }

    public void updateWarehouseFromDTO(Warehouse warehouse, WarehouseRequestDTO dto) {
        if (dto == null) return;

        warehouse.setName(dto.getName());
        warehouse.setCode(dto.getCode());
        warehouse.setAddress(dto.getAddress());
        warehouse.setCity(dto.getCity());
        warehouse.setState(dto.getState());
        warehouse.setCountry(dto.getCountry());
        warehouse.setPostalCode(dto.getPostalCode());
        warehouse.setManagerName(dto.getManagerName());
        warehouse.setManagerEmail(dto.getManagerEmail());
        warehouse.setManagerPhone(dto.getManagerPhone());
        if (dto.getIsActive() != null) warehouse.setIsActive(dto.getIsActive());
        if (dto.getIsDefault() != null) warehouse.setIsDefault(dto.getIsDefault());
    }

    public WarehouseDTO toWarehouseDTO(Warehouse warehouse) {
        if (warehouse == null) return null;

        return WarehouseDTO.builder()
                .id(warehouse.getId())
                .name(warehouse.getName())
                .code(warehouse.getCode())
                .address(warehouse.getAddress())
                .city(warehouse.getCity())
                .state(warehouse.getState())
                .country(warehouse.getCountry())
                .postalCode(warehouse.getPostalCode())
                .managerName(warehouse.getManagerName())
                .managerEmail(warehouse.getManagerEmail())
                .managerPhone(warehouse.getManagerPhone())
                .isActive(warehouse.getIsActive())
                .isDefault(warehouse.getIsDefault())
                .createdAt(warehouse.getCreatedAt())
                .updatedAt(warehouse.getUpdatedAt())
                .build();
    }

    // ==================== Inventory Mapping ====================

    public Inventory toInventoryEntity(InventoryRequestDTO dto, Product product, Warehouse warehouse) {
        if (dto == null) return null;

        return Inventory.builder()
                .product(product)
                .warehouse(warehouse)
                .quantity(dto.getQuantity() != null ? dto.getQuantity() : 0)
                .reserved(dto.getReserved() != null ? dto.getReserved() : 0)
                .lowStockThreshold(dto.getLowStockThreshold() != null ? dto.getLowStockThreshold() : 10)
                .reorderPoint(dto.getReorderPoint())
                .reorderQuantity(dto.getReorderQuantity())
                .maxStockLevel(dto.getMaxStockLevel())
                .safetyStock(dto.getSafetyStock())
                .unitCost(dto.getUnitCost())
                .binLocation(dto.getBinLocation())
                .aisle(dto.getAisle())
                .zone(dto.getZone())
                .trackInventory(dto.getTrackInventory() != null ? dto.getTrackInventory() : true)
                .build();
    }

    public void updateInventoryFromDTO(Inventory inventory, InventoryRequestDTO dto) {
        if (dto == null) return;

        if (dto.getQuantity() != null) inventory.setQuantity(dto.getQuantity());
        if (dto.getReserved() != null) inventory.setReserved(dto.getReserved());
        if (dto.getLowStockThreshold() != null) inventory.setLowStockThreshold(dto.getLowStockThreshold());
        if (dto.getReorderPoint() != null) inventory.setReorderPoint(dto.getReorderPoint());
        if (dto.getReorderQuantity() != null) inventory.setReorderQuantity(dto.getReorderQuantity());
        if (dto.getMaxStockLevel() != null) inventory.setMaxStockLevel(dto.getMaxStockLevel());
        if (dto.getSafetyStock() != null) inventory.setSafetyStock(dto.getSafetyStock());
        if (dto.getUnitCost() != null) inventory.setUnitCost(dto.getUnitCost());
        if (dto.getBinLocation() != null) inventory.setBinLocation(dto.getBinLocation());
        if (dto.getAisle() != null) inventory.setAisle(dto.getAisle());
        if (dto.getZone() != null) inventory.setZone(dto.getZone());
        if (dto.getTrackInventory() != null) inventory.setTrackInventory(dto.getTrackInventory());
    }

    public InventoryDTO toInventoryDTO(Inventory inventory) {
        if (inventory == null) return null;

        return InventoryDTO.builder()
                .id(inventory.getId())
                .productId(inventory.getProduct() != null ? inventory.getProduct().getId() : null)
                .warehouseId(inventory.getWarehouse() != null ? inventory.getWarehouse().getId() : null)
                .product(toProductSummaryDTO(inventory.getProduct()))
                .warehouse(toWarehouseDTO(inventory.getWarehouse()))
                .quantity(inventory.getQuantity())
                .reserved(inventory.getReserved())
                .available(inventory.getAvailable())
                .incoming(inventory.getIncoming())
                .lowStockThreshold(inventory.getLowStockThreshold())
                .reorderPoint(inventory.getReorderPoint())
                .reorderQuantity(inventory.getReorderQuantity())
                .maxStockLevel(inventory.getMaxStockLevel())
                .safetyStock(inventory.getSafetyStock())
                .unitCost(inventory.getUnitCost())
                .totalValue(inventory.getTotalValue())
                .binLocation(inventory.getBinLocation())
                .aisle(inventory.getAisle())
                .zone(inventory.getZone())
                .trackInventory(inventory.getTrackInventory())
                .status(inventory.getStatus() != null ? inventory.getStatus().name() : null)
                .lastRestocked(inventory.getLastRestocked())
                .lastCounted(inventory.getLastCounted())
                .createdAt(inventory.getCreatedAt())
                .updatedAt(inventory.getUpdatedAt())
                .build();
    }

    // ==================== StockMovement Mapping ====================

    public StockMovementDTO toStockMovementDTO(StockMovement movement) {
        if (movement == null) return null;

        return StockMovementDTO.builder()
                .id(movement.getId())
                .inventoryId(movement.getInventory() != null ? movement.getInventory().getId() : null)
                .productId(movement.getProduct() != null ? movement.getProduct().getId() : null)
                .warehouseId(movement.getWarehouse() != null ? movement.getWarehouse().getId() : null)
                .productName(movement.getProduct() != null ? movement.getProduct().getName() : null)
                .warehouseName(movement.getWarehouse() != null ? movement.getWarehouse().getName() : null)
                .type(movement.getType() != null ? movement.getType().name() : null)
                .quantity(movement.getQuantity())
                .previousQuantity(movement.getPreviousQuantity())
                .newQuantity(movement.getNewQuantity())
                .reason(movement.getReason())
                .notes(movement.getNotes())
                .reference(movement.getReference())
                .referenceType(movement.getReferenceType() != null ? movement.getReferenceType().name() : null)
                .createdBy(movement.getCreatedBy())
                .createdAt(movement.getCreatedAt())
                .build();
    }

    // ==================== Helper Methods ====================

    private ProductSummaryDTO toProductSummaryDTO(Product product) {
        if (product == null) return null;

        return ProductSummaryDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .sku(product.getSku())
                .category(product.getCategory())
                .brand(product.getBrand())
                .price(product.getPrice())
                .stock(product.getStock())
                .image(product.getImageUrl())
                .status(product.getStatus())
                .hasOrders(product.getHasOrders())
                .build();
    }

    // ==================== List Mapping ====================

    public List<WarehouseDTO> toWarehouseDTOList(List<Warehouse> warehouses) {
        return warehouses.stream()
                .map(this::toWarehouseDTO)
                .collect(Collectors.toList());
    }

    public List<InventoryDTO> toInventoryDTOList(List<Inventory> inventories) {
        return inventories.stream()
                .map(this::toInventoryDTO)
                .collect(Collectors.toList());
    }

    public List<StockMovementDTO> toStockMovementDTOList(List<StockMovement> movements) {
        return movements.stream()
                .map(this::toStockMovementDTO)
                .collect(Collectors.toList());
    }
}
